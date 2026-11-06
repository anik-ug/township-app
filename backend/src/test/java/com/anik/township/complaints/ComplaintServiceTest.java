package com.anik.township.complaints;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.anik.township.config.DateProvider;
import com.anik.township.domain.*;
import com.anik.township.repository.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ComplaintServiceTest {
    private ComplaintRepository complaints;
    private ComplaintEventRepository events;
    private AppUserRepository users;
    private ComplaintService service;
    private AppUser resident;
    private AppUser helpdeskOne;
    private AppUser helpdeskTwo;
    private final List<ComplaintEvent> savedEvents = new ArrayList<>();

    @BeforeEach
    void setUp() {
        complaints = mock(ComplaintRepository.class);
        events = mock(ComplaintEventRepository.class);
        users = mock(AppUserRepository.class);
        KeywordClassifier keyword = new KeywordClassifier(
                "water,leak", "power,electric", "lift,elevator",
                "security,guard", "clean,garbage", "parking,car");
        LlmClassifier classifier = new LlmClassifier(keyword, mock(ClassifierHttpClient.class),
                new com.fasterxml.jackson.databind.ObjectMapper(), "", "http://unused", "unused");
        SafetyRuleEngine safety = new SafetyRuleEngine("fire,gas leak");
        DateProvider clock = new DateProvider(
                Clock.fixed(Instant.parse("2026-11-06T07:00:00Z"), ZoneId.of("UTC")), "");
        resident = user(10L, "Resident", Role.RESIDENT, "T01");
        helpdeskOne = user(41L, "Helpdesk One", Role.HELPDESK, null);
        helpdeskTwo = user(42L, "Helpdesk Two", Role.HELPDESK, null);
        when(users.findAll()).thenReturn(List.of(helpdeskOne, helpdeskTwo));
        when(users.findByPhone("9100000001")).thenReturn(Optional.empty());
        when(complaints.countByAssigneeIdAndStatusIn(41L, Set.of(
                ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS))).thenReturn(3L);
        when(complaints.countByAssigneeIdAndStatusIn(42L, Set.of(
                ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS))).thenReturn(1L);
        when(complaints.findByFlat_TowerAndCategoryAndStatusNotInAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
                anyString(), anyString(), any(), any())).thenReturn(List.of());
        when(events.save(any(ComplaintEvent.class))).thenAnswer(invocation -> {
            ComplaintEvent event = invocation.getArgument(0);
            savedEvents.add(event);
            return event;
        });
        when(events.findAll()).thenAnswer(invocation -> List.copyOf(savedEvents));
        service = new ComplaintService(complaints, events, users, mock(FlatRepository.class),
                classifier, safety, clock, "9100000001");
    }

    @Test
    void roundRobinChoosesHelpdeskWithFewestOpenComplaints() {
        when(complaints.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        ComplaintService.View view = service.create(resident, "Water leak in kitchen pipe", null);

        assertEquals(42L, view.assigneeId());
    }

    @Test
    void duplicateIsLinkedWithinTwoHours() {
        Complaint existing = complaint(1L, ComplaintStatus.OPEN, OffsetDateTime.parse("2026-11-06T06:00:00Z"));
        when(complaints.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint c = invocation.getArgument(0);
            c.setId(c.getId() == null ? 2L : c.getId());
            return c;
        });
        when(complaints.findByFlat_TowerAndCategoryAndStatusNotInAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
                anyString(), anyString(), any(), any())).thenReturn(List.of(existing));

        ComplaintService.View view = service.create(resident, "Water leak in kitchen pipe", null);

        assertEquals(1L, view.duplicateOf());
    }

    @Test
    void duplicateIsNotLinkedAfterTwoHours() {
        Complaint existing = complaint(1L, ComplaintStatus.OPEN, OffsetDateTime.parse("2026-11-06T04:59:59Z"));
        when(complaints.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });
        when(complaints.findByFlat_TowerAndCategoryAndStatusNotInAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
                anyString(), anyString(), any(), any())).thenReturn(List.of());

        assertNull(service.create(resident, "Water leak in kitchen pipe", null).duplicateOf());
    }

    @Test
    void duplicateCannotBeClosed() {
        Complaint duplicate = complaint(3L, ComplaintStatus.RESOLVED, OffsetDateTime.now());
        duplicate.setDuplicateOf(complaint(1L, ComplaintStatus.OPEN, OffsetDateTime.now()));
        when(complaints.findById(3L)).thenReturn(Optional.of(duplicate));

        assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(3L, ComplaintStatus.CLOSED, helpdeskOne));
    }

    @Test
    void allValidStatusTransitionsAreAllowed() {
        for (StatusPair pair : List.of(
                new StatusPair(ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED),
                new StatusPair(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS),
                new StatusPair(ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED),
                new StatusPair(ComplaintStatus.RESOLVED, ComplaintStatus.IN_PROGRESS),
                new StatusPair(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED))) {
            Complaint c = complaint(20L, pair.from(), OffsetDateTime.now());
            when(complaints.findById(20L)).thenReturn(Optional.of(c));
            assertDoesNotThrow(() -> service.changeStatus(20L, pair.to(), helpdeskOne), pair.toString());
        }
    }

    @Test
    void invalidStatusTransitionsReturnConflict() {
        for (ComplaintStatus from : ComplaintStatus.values()) {
            for (ComplaintStatus to : ComplaintStatus.values()) {
                boolean valid = (from == ComplaintStatus.OPEN && to == ComplaintStatus.ASSIGNED)
                        || (from == ComplaintStatus.ASSIGNED && to == ComplaintStatus.IN_PROGRESS)
                        || (from == ComplaintStatus.IN_PROGRESS && to == ComplaintStatus.RESOLVED)
                        || (from == ComplaintStatus.RESOLVED && to == ComplaintStatus.IN_PROGRESS)
                        || (from == ComplaintStatus.RESOLVED && to == ComplaintStatus.CLOSED);
                if (!valid) {
                    Complaint c = complaint(21L, from, OffsetDateTime.now());
                    when(complaints.findById(21L)).thenReturn(Optional.of(c));
                    assertThrows(ResponseStatusException.class,
                            () -> service.changeStatus(21L, to, helpdeskOne), from + " -> " + to);
                }
            }
        }
    }

    @Test
    void categoryChangeSetsHumanAndKeepsEscalation() {
        Complaint c = complaint(4L, ComplaintStatus.ASSIGNED, OffsetDateTime.now());
        c.setEscalated(true);
        c.setEscalationReason("Safety keyword: fire");
        when(complaints.findById(4L)).thenReturn(Optional.of(c));

        ComplaintService.View view = service.changeCategory(4L, "ELECTRICAL", helpdeskOne);

        assertEquals(CategorySource.HUMAN, c.getCategorySource());
        assertTrue(c.isEscalated());
        assertEquals("Safety keyword: fire", c.getEscalationReason());
        assertEquals(CategorySource.HUMAN, view.categorySource());
    }

    @Test
    void ratingOnlyWorksAfterClosedAndOnlyOnce() {
        Complaint c = complaint(5L, ComplaintStatus.IN_PROGRESS, OffsetDateTime.now());
        when(complaints.findById(5L)).thenReturn(Optional.of(c));
        assertThrows(ResponseStatusException.class, () -> service.rate(10L, 5L, (short) 5));

        c.setStatus(ComplaintStatus.CLOSED);
        assertDoesNotThrow(() -> service.rate(10L, 5L, (short) 5));
        assertThrows(ResponseStatusException.class, () -> service.rate(10L, 5L, (short) 4));
    }

    @Test
    void reopenOnlyWorksFromResolved() {
        Complaint c = complaint(6L, ComplaintStatus.IN_PROGRESS, OffsetDateTime.now());
        when(complaints.findById(6L)).thenReturn(Optional.of(c));
        assertThrows(ResponseStatusException.class, () -> service.reopen(10L, 6L, resident));

        c.setStatus(ComplaintStatus.RESOLVED);
        assertDoesNotThrow(() -> service.reopen(10L, 6L, resident));
        assertEquals(ComplaintStatus.IN_PROGRESS, c.getStatus());
    }

    private Complaint complaint(long id, ComplaintStatus status, OffsetDateTime createdAt) {
        Complaint c = new Complaint();
        c.setId(id);
        c.setStatus(status);
        c.setCreatedAt(createdAt);
        c.setCategory("PLUMBING");
        c.setCategorySource(CategorySource.KEYWORD);
        c.setUrgency(ComplaintUrgency.MEDIUM);
        c.setDescription("Water leak in pipe");
        c.setFlat(resident.getFlat());
        return c;
    }

    private static AppUser user(long id, String name, Role role, String tower) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setName(name);
        user.setPhone(String.valueOf(9000000000L + id));
        user.setRole(role);
        if (role == Role.RESIDENT) {
            Flat flat = new Flat();
            flat.setId(id);
            flat.setTower(tower);
            user.setFlat(flat);
        }
        return user;
    }

    private record StatusPair(ComplaintStatus from, ComplaintStatus to) {}
}
