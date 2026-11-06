package com.anik.township.complaints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.anik.township.config.DateProvider;
import com.anik.township.domain.*;
import com.anik.township.repository.AppUserRepository;
import com.anik.township.repository.ComplaintEventRepository;
import com.anik.township.repository.ComplaintRepository;
import com.anik.township.repository.FlatRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComplaintServiceCreateTest {
    private ComplaintRepository complaints;
    private ComplaintEventRepository events;
    private AppUserRepository users;
    private ComplaintService service;
    private AppUser resident;
    private AppUser helpdesk;
    private AppUser dutyManager;
    private final List<ComplaintEvent> savedEvents = new ArrayList<>();

    @BeforeEach
    void setUp() {
        complaints = mock(ComplaintRepository.class);
        events = mock(ComplaintEventRepository.class);
        users = mock(AppUserRepository.class);
        FlatRepository flats = mock(FlatRepository.class);
        KeywordClassifier keywords = mock(KeywordClassifier.class);
        LlmClassifier classifier = mock(LlmClassifier.class);
        SafetyRuleEngine safety = new SafetyRuleEngine("fire,gas leak");
        DateProvider clock = new DateProvider(
                Clock.fixed(Instant.parse("2026-11-06T07:00:00Z"), ZoneId.of("UTC")), "");

        resident = user(10L, "Resident", Role.RESIDENT, "T01");
        helpdesk = user(41L, "Helpdesk One", Role.HELPDESK, "T99");
        dutyManager = user(42L, "Duty Manager", Role.HELPDESK, "T99");
        when(users.findAll()).thenReturn(List.of(helpdesk, dutyManager));
        when(users.findByPhone("9100000001")).thenReturn(java.util.Optional.of(dutyManager));
        when(complaints.countByAssigneeIdAndStatusIn(anyLong(), any())).thenReturn(0L);
        when(complaints.findByFlat_TowerAndCategoryAndStatusNotInAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
                anyString(), anyString(), any(), any())).thenReturn(List.of());
        when(complaints.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint complaint = invocation.getArgument(0);
            complaint.setId(23L);
            return complaint;
        });
        doAnswer(invocation -> {
            savedEvents.add(invocation.getArgument(0));
            return invocation.getArgument(0);
        }).when(events).save(any(ComplaintEvent.class));
        when(classifier.classify(anyString())).thenReturn(
                new ComplaintClassifier.Result("PLUMBING", ComplaintUrgency.LOW, 80, "keyword"));
        when(classifier.usesLlm()).thenReturn(false);
        when(events.findAll()).thenAnswer(invocation -> List.copyOf(savedEvents));
        service = new ComplaintService(complaints, events, users, flats, classifier, safety, clock, "9100000001");
    }

    @Test
    void normalCreateWritesOpenThenAssignedEvents() {
        ComplaintService.View view = service.create(resident, "Water leak in kitchen pipe", null);

        assertEquals(List.of(ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED),
                savedEvents.stream().map(ComplaintEvent::getToStatus).toList());
        assertEquals("Assigned to Helpdesk One", savedEvents.get(1).getNote());
        assertEquals(List.of("Complaint created", "Assigned to Helpdesk One"),
                view.timeline().stream().map(ComplaintService.EventView::note).toList());
    }

    @Test
    void safetyCreateWritesEscalationDecisionAfterAssignment() {
        ComplaintService.View view = service.create(resident, "There is a fire in the kitchen", null);

        assertEquals(3, savedEvents.size());
        assertEquals(List.of(ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED, ComplaintStatus.ASSIGNED),
                savedEvents.stream().map(ComplaintEvent::getToStatus).toList());
        assertEquals("Assigned to Duty Manager", savedEvents.get(1).getNote());
        assertEquals("Escalated to duty manager: Safety keyword fire", savedEvents.get(2).getNote());
        assertEquals(ComplaintStatus.ASSIGNED, savedEvents.get(2).getToStatus());
        assertEquals(List.of("Complaint created", "Assigned to Duty Manager",
                        "Escalated to duty manager: Safety keyword fire"),
                view.timeline().stream().map(ComplaintService.EventView::note).toList());
    }

    @Test
    void helpdeskListRanksOpenLowAheadOfClosedCriticalEscalated() {
        Complaint closedCritical = complaint(1L, ComplaintStatus.CLOSED, ComplaintUrgency.CRITICAL, true,
                "2026-11-01T10:00:00Z");
        Complaint openLow = complaint(2L, ComplaintStatus.OPEN, ComplaintUrgency.LOW, false,
                "2026-11-06T10:00:00Z");
        when(complaints.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(List.of(closedCritical, openLow));

        assertEquals(List.of(2L, 1L), service.helpdeskList(null, null, null, null, null)
                .stream().map(ComplaintService.View::id).toList());
    }

    private static Complaint complaint(long id, ComplaintStatus status, ComplaintUrgency urgency,
            boolean escalated, String createdAt) {
        Complaint complaint = new Complaint();
        complaint.setId(id);
        complaint.setStatus(status);
        complaint.setUrgency(urgency);
        complaint.setEscalated(escalated);
        complaint.setCategory("OTHER");
        complaint.setCategorySource(CategorySource.KEYWORD);
        complaint.setDescription("Complaint description");
        complaint.setCreatedAt(java.time.OffsetDateTime.parse(createdAt));
        Flat flat = new Flat();
        flat.setTower("T01");
        complaint.setFlat(flat);
        return complaint;
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
}
