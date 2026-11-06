package com.anik.township.complaints;

import com.anik.township.auth.AuthUser;
import com.anik.township.config.DateProvider;
import com.anik.township.domain.*;
import com.anik.township.repository.AppUserRepository;
import com.anik.township.repository.ComplaintEventRepository;
import com.anik.township.repository.ComplaintRepository;
import com.anik.township.repository.FlatRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComplaintService {
    private static final Set<ComplaintStatus> OPEN = Set.of(
            ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS);
    private final ComplaintRepository complaints;
    private final ComplaintEventRepository events;
    private final AppUserRepository users;
    private final FlatRepository flats;
    private final LlmClassifier classifier;
    private final SafetyRuleEngine safety;
    private final DateProvider clock;
    private final String dutyManagerPhone;

    public ComplaintService(ComplaintRepository complaints, ComplaintEventRepository events,
            AppUserRepository users, FlatRepository flats, LlmClassifier classifier, SafetyRuleEngine safety,
            DateProvider clock, @Value("${app.duty-manager-phone}") String dutyManagerPhone) {
        this.complaints = complaints;
        this.events = events;
        this.users = users;
        this.flats = flats;
        this.classifier = classifier;
        this.safety = safety;
        this.clock = clock;
        this.dutyManagerPhone = dutyManagerPhone;
    }

    @Transactional
    public View create(AppUser resident, String description, String requestedCategory) {
        ComplaintClassifier.Result result = classifier.classify(description);
        SafetyRuleEngine.Result rule = safety.apply(description);
        Complaint c = new Complaint();
        c.setFlat(resident.getFlat());
        c.setRaisedBy(resident);
        c.setDescription(description);
        c.setCategory(requestedCategory == null ? result.category() : requestedCategory);
        c.setUrgency(rule.escalated() ? ComplaintUrgency.CRITICAL : result.urgency());
        c.setCategorySource(rule.escalated() ? CategorySource.RULE
                : classifier.usesLlm() ? CategorySource.AI : CategorySource.KEYWORD);
        c.setEscalated(rule.escalated());
        c.setEscalationReason(rule.reason());
        c.setStatus(ComplaintStatus.ASSIGNED);
        c.setCreatedAt(clock.now());
        c.setAssignee(assignNext());
        if (rule.escalated()) {
            users.findByPhone(dutyManagerPhone).filter(u -> u.getRole() == Role.HELPDESK).ifPresent(u -> {
                c.setAssignee(u);
                System.out.println("MOCK NOTIFICATION duty manager=" + u.getPhone() + " complaint=safety");
            });
        }
        complaints.save(c);
        complaints.findByFlat_TowerAndCategoryAndStatusNotInAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
                c.getFlat().getTower(), c.getCategory(), List.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED),
                c.getCreatedAt().minusHours(2)).stream().filter(existing -> !existing.getId().equals(c.getId()))
                .findFirst().ifPresent(c::setDuplicateOf);
        event(c, null, ComplaintStatus.OPEN, resident, "Complaint created");
        event(c, ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED, c.getAssignee(),
                "Assigned to " + c.getAssignee().getName());
        if (rule.escalated()) {
            event(c, ComplaintStatus.ASSIGNED, ComplaintStatus.ASSIGNED, c.getAssignee(),
                    "Escalated to duty manager: " + rule.reason().replace("Safety keyword: ", "Safety keyword "));
        }
        return view(c);
    }

    private AppUser assignNext() {
        return users.findAll().stream().filter(u -> u.getRole() == Role.HELPDESK)
                .min(Comparator.<AppUser>comparingLong(u -> complaints.countByAssigneeIdAndStatusIn(u.getId(), OPEN))
                        .thenComparing(AppUser::getId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No helpdesk users available"));
    }

    @Transactional
    public List<View> residentList(long flatId) {
        return complaints.findByFlatIdOrderByCreatedAtDesc(flatId).stream().map(this::view).toList();
    }

    @Transactional
    public View residentGet(long flatId, long id) {
        Complaint c = get(id);
        if (!c.getFlat().getId().equals(flatId)) throw notFound();
        return view(c);
    }

    @Transactional
    public View reopen(long flatId, long id, AppUser actor) {
        Complaint c = getForFlat(flatId, id);
        if (c.getStatus() != ComplaintStatus.RESOLVED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only resolved complaints can be reopened");
        ComplaintStatus from = c.getStatus();
        c.setStatus(ComplaintStatus.IN_PROGRESS);
        c.setReopenCount(c.getReopenCount() + 1);
        event(c, from, c.getStatus(), actor, "Complaint reopened");
        return view(c);
    }

    @Transactional
    public View rate(long flatId, long id, short rating) {
        Complaint c = getForFlat(flatId, id);
        if (c.getStatus() != ComplaintStatus.CLOSED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only closed complaints can be rated");
        if (c.getRating() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Complaint is already rated");
        c.setRating(rating);
        return view(c);
    }

    @Transactional
    public List<View> helpdeskList(String status, String category, String urgency, Boolean escalated, String tower) {
        return complaints.findByStatusInOrderByCreatedAtAsc(List.of(ComplaintStatus.values())).stream()
                .filter(c -> status == null || c.getStatus().name().equals(status))
                .filter(c -> category == null || c.getCategory().equals(category))
                .filter(c -> urgency == null || c.getUrgency().name().equals(urgency))
                .filter(c -> escalated == null || c.isEscalated() == escalated)
                .filter(c -> tower == null || c.getFlat().getTower().equals(tower))
                .sorted(Comparator.comparingInt((Complaint c) -> statusRank(c.getStatus()))
                        .thenComparing(Comparator.comparing(Complaint::isEscalated).reversed())
                        .thenComparing(Complaint::getUrgency, Comparator.reverseOrder())
                        .thenComparing(Complaint::getCreatedAt))
                .map(this::view).toList();
    }

    private static int statusRank(ComplaintStatus status) {
        return switch (status) {
            case OPEN, ASSIGNED, IN_PROGRESS -> 0;
            case RESOLVED -> 1;
            case CLOSED -> 2;
        };
    }

    @Transactional
    public View changeCategory(long id, String category, AppUser actor) {
        Complaint c = get(id);
        c.setCategory(category);
        c.setCategorySource(CategorySource.HUMAN);
        event(c, c.getStatus(), c.getStatus(), actor, "Category changed to " + category);
        return view(c);
    }

    @Transactional
    public View changeStatus(long id, ComplaintStatus status, AppUser actor) {
        Complaint c = get(id);
        ComplaintStatus from = c.getStatus();
        if (status == ComplaintStatus.CLOSED && c.getDuplicateOf() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate complaints cannot be closed");
        }
        if (!((from == ComplaintStatus.OPEN && status == ComplaintStatus.ASSIGNED)
                || (from == ComplaintStatus.ASSIGNED && status == ComplaintStatus.IN_PROGRESS)
                || (from == ComplaintStatus.IN_PROGRESS && status == ComplaintStatus.RESOLVED)
                || (from == ComplaintStatus.RESOLVED && status == ComplaintStatus.IN_PROGRESS)
                || (from == ComplaintStatus.RESOLVED && status == ComplaintStatus.CLOSED))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid complaint status transition");
        }
        c.setStatus(status);
        if (from == ComplaintStatus.OPEN || from == ComplaintStatus.ASSIGNED) c.setFirstResponseAt(clock.now());
        if (status == ComplaintStatus.CLOSED) c.setClosedAt(clock.now());
        event(c, from, status, actor, "Status changed");
        return view(c);
    }

    @Transactional
    public View assign(long id, long assigneeId, AppUser actor) {
        Complaint c = get(id);
        AppUser assignee = users.findById(assigneeId)
                .filter(u -> u.getRole() == Role.HELPDESK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee must be a helpdesk user"));
        c.setAssignee(assignee);
        event(c, c.getStatus(), c.getStatus(), actor, "Complaint assigned");
        return view(c);
    }

    private Complaint getForFlat(long flatId, long id) {
        Complaint c = get(id);
        if (!c.getFlat().getId().equals(flatId)) throw notFound();
        return c;
    }
    private Complaint get(long id) {
        return complaints.findById(id).orElseThrow(this::notFound);
    }
    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");
    }
    private void event(Complaint c, ComplaintStatus from, ComplaintStatus to, AppUser actor, String note) {
        ComplaintEvent e = new ComplaintEvent();
        e.setComplaint(c); e.setFromStatus(from); e.setToStatus(to); e.setActor(actor); e.setNote(note); e.setEventAt(clock.now());
        events.save(e);
    }
    private View view(Complaint c) {
        return new View(c.getId(), c.getDescription(), c.getCategory(), c.getUrgency(), c.getCategorySource(),
                c.getStatus(), c.getAssignee() == null ? null : c.getAssignee().getId(), c.getDuplicateOf() == null ? null : c.getDuplicateOf().getId(),
                c.isEscalated(), c.getEscalationReason(), c.getReopenCount(), c.getRating(), c.getFirstResponseAt(), c.getClosedAt(),
                c.getCreatedAt(), events.findAll().stream().filter(e -> e.getComplaint().getId().equals(c.getId()))
                .sorted(Comparator.comparing(ComplaintEvent::getEventAt)).map(e -> new EventView(e.getFromStatus(), e.getToStatus(), e.getNote(), e.getEventAt())).toList());
    }

    public record View(Long id, String description, String category, ComplaintUrgency urgency, CategorySource categorySource,
            ComplaintStatus status, Long assigneeId, Long duplicateOf, boolean escalated, String escalationReason,
            int reopenCount, Short rating, OffsetDateTime firstResponseAt, OffsetDateTime closedAt, OffsetDateTime createdAt,
            List<EventView> timeline) {}
    public record EventView(ComplaintStatus fromStatus, ComplaintStatus toStatus, String note, OffsetDateTime at) {}
}
