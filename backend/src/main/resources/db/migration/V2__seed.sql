INSERT INTO flat (id, tower, flat_no, area_sqft, upi_id) VALUES
  (1, 'T07', 'T07-1203', 1150, 'neeraj.k@okbank'),
  (2, 'T12', 'T12-0408', 980, 'swati.j@okbank'),
  (3, 'T21', 'T21-1601', 1640, 'aamir.s@okbank'),
  (4, 'T01', 'T01-0101', 700, 'rohan.p@okbank'),
  (5, 'T02', 'T02-0202', 820, 'meera.r@okbank'),
  (6, 'T03', 'T03-0303', 930, 'kabir.s@okbank'),
  (7, 'T04', 'T04-0401', 1080, 'liftdesk@okbank'),
  (8, 'T04', 'T04-0402', 1100, 'liftdesk@okbank'),
  (9, 'T04', 'T04-0501', 1250, NULL),
  (10, 'T04', 'T04-0502', 1320, NULL),
  (11, 'T04', 'T04-0601', 1450, NULL),
  (12, 'T05', 'T05-0701', 760, 'devika.m@okbank'),
  (13, 'T06', 'T06-0802', 880, NULL),
  (14, 'T08', 'T08-0901', 1010, NULL),
  (15, 'T09', 'T09-1002', 1180, 'samar.k@okbank'),
  (16, 'T10', 'T10-1101', 1270, NULL),
  (17, 'T11', 'T11-1202', 1390, NULL),
  (18, 'T13', 'T13-1301', 1500, 'tara.n@okbank'),
  (19, 'T14', 'T14-1402', 1580, NULL),
  (20, 'T15', 'T15-1501', 1620, NULL),
  (21, 'T16', 'T16-1602', 1700, 'harsh.v@okbank'),
  (22, 'T17', 'T17-1701', 1800, NULL),
  (23, 'T18', 'T18-1802', 740, NULL),
  (24, 'T19', 'T19-1901', 860, 'naina.g@okbank'),
  (25, 'T20', 'T20-2002', 970, NULL),
  (26, 'T22', 'T22-2101', 1060, NULL),
  (27, 'T23', 'T23-2202', 1140, 'omkar.d@okbank'),
  (28, 'T24', 'T24-2301', 1210, NULL),
  (29, 'T25', 'T25-2402', 1300, NULL),
  (30, 'T26', 'T26-2501', 1400, NULL),
  (31, 'T27', 'T27-2602', 1480, 'rahul.b@okbank'),
  (32, 'T28', 'T28-2701', 1550, NULL),
  (33, 'T01', 'T01-0102', 780, NULL),
  (34, 'T02', 'T02-0203', 900, NULL),
  (35, 'T03', 'T03-0304', 1040, NULL),
  (36, 'T05', 'T05-0702', 1160, NULL),
  (37, 'T06', 'T06-0803', 1280, NULL),
  (38, 'T08', 'T08-0902', 1360, NULL),
  (39, 'T09', 'T09-1003', 1520, NULL),
  (40, 'T10', 'T10-1102', 1680, NULL);

INSERT INTO app_user (id, phone, name, role, flat_id, is_tenant) VALUES
  (1, '9000000001', 'Neeraj Kulkarni', 'RESIDENT', 1, FALSE),
  (2, '9000000002', 'Swati Joshi', 'RESIDENT', 2, TRUE),
  (3, '9000000003', 'Aamir Shaikh', 'RESIDENT', 3, FALSE),
  (4, '9000000004', 'Rohan Patil', 'RESIDENT', 4, FALSE),
  (5, '9000000005', 'Meera Rao', 'RESIDENT', 5, FALSE),
  (6, '9000000006', 'Kabir Shah', 'RESIDENT', 6, FALSE),
  (7, '9000000007', 'Anaya Deshmukh', 'RESIDENT', 7, FALSE),
  (8, '9000000008', 'Vikram Bhide', 'RESIDENT', 8, FALSE),
  (9, '9000000009', 'Ira Nair', 'RESIDENT', 9, FALSE),
  (10, '9000000010', 'Sahil More', 'RESIDENT', 10, FALSE),
  (11, '9000000011', 'Pooja Kale', 'RESIDENT', 11, FALSE),
  (12, '9000000012', 'Devika Menon', 'RESIDENT', 12, FALSE),
  (13, '9000000013', 'Arjun Dube', 'RESIDENT', 13, FALSE),
  (14, '9000000014', 'Mitali Sen', 'RESIDENT', 14, FALSE),
  (15, '9000000015', 'Samar Kulkarni', 'RESIDENT', 15, FALSE),
  (16, '9000000016', 'Leena Joshi', 'RESIDENT', 16, FALSE),
  (17, '9000000017', 'Pranav Iyer', 'RESIDENT', 17, FALSE),
  (18, '9000000018', 'Tara Nair', 'RESIDENT', 18, FALSE),
  (19, '9000000019', 'Nikhil Gokhale', 'RESIDENT', 19, FALSE),
  (20, '9000000020', 'Rhea Shah', 'RESIDENT', 20, FALSE),
  (21, '9000000021', 'Harsh Vora', 'RESIDENT', 21, FALSE),
  (22, '9000000022', 'Aniket Rao', 'RESIDENT', 22, FALSE),
  (23, '9000000023', 'Maya Pillai', 'RESIDENT', 23, FALSE),
  (24, '9000000024', 'Naina Gupta', 'RESIDENT', 24, FALSE),
  (25, '9000000025', 'Yash Mehta', 'RESIDENT', 25, FALSE),
  (26, '9000000026', 'Ishita Roy', 'RESIDENT', 26, FALSE),
  (27, '9000000027', 'Omkar Desai', 'RESIDENT', 27, FALSE),
  (28, '9000000028', 'Aditi Kamat', 'RESIDENT', 28, FALSE),
  (29, '9000000029', 'Kunal Sethi', 'RESIDENT', 29, FALSE),
  (30, '9000000030', 'Simran Gill', 'RESIDENT', 30, FALSE),
  (31, '9000000031', 'Rahul Bose', 'RESIDENT', 31, FALSE),
  (32, '9000000032', 'Tanvi Rane', 'RESIDENT', 32, FALSE),
  (33, '9000000033', 'Aarav Kulkarni', 'RESIDENT', 33, FALSE),
  (34, '9000000034', 'Kavya Shah', 'RESIDENT', 34, FALSE),
  (35, '9000000035', 'Mihir Joshi', 'RESIDENT', 35, FALSE),
  (36, '9000000036', 'Sana Khan', 'RESIDENT', 36, FALSE),
  (37, '9000000037', 'Adwait Limaye', 'RESIDENT', 37, FALSE),
  (38, '9000000038', 'Ritu Verma', 'RESIDENT', 38, FALSE),
  (39, '9000000039', 'Neil Fernandes', 'RESIDENT', 39, FALSE),
  (40, '9000000040', 'Gauri Naik', 'RESIDENT', 40, FALSE),
  (41, '9100000001', 'Mohan Bapat', 'HELPDESK', NULL, FALSE),
  (42, '9100000002', 'Farah Qureshi', 'HELPDESK', NULL, FALSE),
  (43, '9100000003', 'Suresh Iyer', 'ACCOUNTS', NULL, FALSE),
  (44, '9100000004', 'Kiran Salvi', 'COMMITTEE', NULL, FALSE),
  (45, '9100000005', 'Lata Menon', 'HELPDESK', NULL, FALSE);

INSERT INTO late_fee_rule (id, grace_days, annual_rate_bps, cap_paise, active)
VALUES (1, 10, 1200, NULL, TRUE);

INSERT INTO bill (id, flat_id, bill_month, amount_paise, due_date)
SELECT ((f.id - 1) * 3) + m.seq,
       f.id,
       m.bill_month,
       f.area_sqft * 650,
       CASE
         WHEN f.id = 2 AND m.seq = 2 THEN DATE '2026-09-19'
         WHEN f.id = 3 AND m.seq = 3 THEN DATE '2026-10-18'
         ELSE m.bill_month + 9
       END
FROM flat f
CROSS JOIN (VALUES
  (1, DATE '2026-08-01'),
  (2, DATE '2026-09-01'),
  (3, DATE '2026-10-01')
) AS m(seq, bill_month);

INSERT INTO payment (id, bill_id, amount_paise, method, reference, paid_at)
SELECT ROW_NUMBER() OVER (ORDER BY b.id),
       b.id,
       b.amount_paise,
       CASE WHEN b.id % 5 = 0 THEN 'MANUAL' ELSE 'TEST_ONLINE' END,
       'SEED-' || b.id,
       CASE b.bill_month
         WHEN DATE '2026-08-01' THEN TIMESTAMPTZ '2026-08-05 10:00:00+05:30' + (b.flat_id % 8) * INTERVAL '1 day'
         ELSE TIMESTAMPTZ '2026-09-05 10:00:00+05:30' + (b.flat_id % 9) * INTERVAL '1 day'
       END
FROM bill b
WHERE b.bill_month IN (DATE '2026-08-01', DATE '2026-09-01')
  AND NOT (b.flat_id = 2 AND b.bill_month = DATE '2026-09-01');

INSERT INTO payment (id, bill_id, amount_paise, method, reference, paid_at)
SELECT 81 + ROW_NUMBER() OVER (ORDER BY b.id),
       b.id,
       CASE
         WHEN b.flat_id = 3 THEN 500000
         WHEN b.flat_id IN (28, 29, 30, 31, 32) THEN b.amount_paise / 2
         ELSE b.amount_paise
       END,
       CASE WHEN b.flat_id % 4 = 0 THEN 'MANUAL' ELSE 'TEST_ONLINE' END,
       'SEED-OCT-' || b.flat_id,
       TIMESTAMPTZ '2026-10-06 11:00:00+05:30' + (b.flat_id % 7) * INTERVAL '1 day'
FROM bill b
WHERE b.bill_month = DATE '2026-10-01'
  AND b.flat_id <= 32;

INSERT INTO complaint (id, flat_id, raised_by, description, category, urgency, category_source, status,
                       assignee_id, duplicate_of, escalated, escalation_reason, created_at, closed_at)
VALUES
  (1, 2, 2, 'water leak in bathroom ceiling', 'PLUMBING', 'HIGH', 'RULE', 'OPEN', NULL, NULL, FALSE, NULL, '2026-11-04 09:15:00+05:30', NULL),
  (2, 7, 7, 'Tower T04 lift makes a loud grinding noise', 'LIFT', 'HIGH', 'HUMAN', 'ASSIGNED', 41, NULL, FALSE, NULL, '2026-10-09 08:20:00+05:30', NULL),
  (3, 8, 8, 'Same T04 lift is stopping between floors', 'LIFT', 'HIGH', 'HUMAN', 'IN_PROGRESS', 42, 2, FALSE, NULL, '2026-10-12 17:40:00+05:30', NULL),
  (4, 9, 9, 'Lift doors shudder and reopen repeatedly', 'LIFT', 'MEDIUM', 'AI', 'RESOLVED', 41, 2, FALSE, NULL, '2026-10-15 12:10:00+05:30', NULL),
  (5, 10, 10, 'T04 passenger lift is stuck at the lobby', 'LIFT', 'HIGH', 'RULE', 'CLOSED', 42, 2, TRUE, 'duplicate lift report', '2026-10-20 08:05:00+05:30', '2026-10-21 16:30:00+05:30'),
  (6, 11, 11, 'Repeated lift vibration in T04', 'LIFT', 'MEDIUM', 'AI', 'CLOSED', 41, 2, TRUE, 'duplicate lift report', '2026-10-22 18:25:00+05:30', '2026-10-24 11:00:00+05:30'),
  (7, 12, 12, 'smell of gas in kitchen', 'OTHER', 'CRITICAL', 'RULE', 'ASSIGNED', 42, NULL, TRUE, 'gas safety keyword', '2026-11-01 19:45:00+05:30', NULL),
  (8, 3, 3, 'Power trips when using the microwave', 'ELECTRICAL', 'HIGH', 'AI', 'IN_PROGRESS', 41, NULL, FALSE, NULL, '2026-10-28 14:30:00+05:30', NULL),
  (9, 4, 4, 'Water pressure is low in the kitchen', 'PLUMBING', 'MEDIUM', 'AI', 'RESOLVED', 42, NULL, FALSE, NULL, '2026-10-18 10:00:00+05:30', NULL),
  (10, 5, 5, 'Visitor parking slot is blocked overnight', 'PARKING', 'LOW', 'HUMAN', 'CLOSED', 41, NULL, FALSE, NULL, '2026-10-10 20:10:00+05:30', '2026-10-11 09:00:00+05:30'),
  (11, 6, 6, 'Unknown person tried the service entrance', 'SECURITY', 'HIGH', 'RULE', 'ASSIGNED', 42, NULL, TRUE, 'security keyword', '2026-10-30 22:00:00+05:30', NULL),
  (12, 13, 13, 'Corridor lights are flickering', 'ELECTRICAL', 'LOW', 'AI', 'OPEN', NULL, NULL, FALSE, NULL, '2026-11-02 07:25:00+05:30', NULL),
  (13, 14, 14, 'Garbage collection missed for two days', 'HOUSEKEEPING', 'MEDIUM', 'AI', 'IN_PROGRESS', 41, NULL, FALSE, NULL, '2026-10-25 09:50:00+05:30', NULL),
  (14, 15, 15, 'Car wash water is pooling near ramp', 'HOUSEKEEPING', 'LOW', 'HUMAN', 'RESOLVED', 42, NULL, FALSE, NULL, '2026-10-14 08:00:00+05:30', NULL),
  (15, 16, 16, 'Stranger followed resident through gate', 'SECURITY', 'CRITICAL', 'RULE', 'CLOSED', 41, NULL, TRUE, 'security keyword', '2026-10-08 21:30:00+05:30', '2026-10-09 00:15:00+05:30'),
  (16, 17, 17, 'Bathroom exhaust fan has stopped', 'ELECTRICAL', 'LOW', 'AI', 'OPEN', NULL, NULL, FALSE, NULL, '2026-10-31 13:15:00+05:30', NULL),
  (17, 18, 18, 'Water seepage near bedroom window', 'PLUMBING', 'MEDIUM', 'AI', 'CLOSED', 42, NULL, FALSE, NULL, '2026-10-11 16:00:00+05:30', '2026-10-13 12:00:00+05:30'),
  (18, 19, 19, 'Delivery vehicles block the fire lane', 'PARKING', 'HIGH', 'RULE', 'ASSIGNED', 41, NULL, TRUE, 'fire lane keyword', '2026-11-03 18:10:00+05:30', NULL),
  (19, 20, 20, 'Clubhouse chair repair request', 'OTHER', 'LOW', 'HUMAN', 'RESOLVED', 42, NULL, FALSE, NULL, '2026-10-19 11:45:00+05:30', NULL),
  (20, 21, 21, 'Common area cleaning schedule unclear', 'HOUSEKEEPING', 'LOW', 'AI', 'OPEN', NULL, NULL, FALSE, NULL, '2026-10-27 07:40:00+05:30', NULL);

INSERT INTO complaint_event (id, complaint_id, actor_id, from_status, to_status, note, at) VALUES
  (1, 1, 2, NULL, 'OPEN', 'Complaint received', '2026-11-04 09:15:00+05:30'),
  (2, 2, 7, NULL, 'OPEN', 'Complaint received', '2026-10-09 08:20:00+05:30'),
  (3, 2, 41, 'OPEN', 'ASSIGNED', 'Lift technician notified', '2026-10-09 10:00:00+05:30'),
  (4, 3, 8, NULL, 'OPEN', 'Complaint received', '2026-10-12 17:40:00+05:30'),
  (5, 3, 42, 'OPEN', 'IN_PROGRESS', 'Linked to lift inspection', '2026-10-13 09:10:00+05:30'),
  (6, 4, 9, NULL, 'OPEN', 'Complaint received', '2026-10-15 12:10:00+05:30'),
  (7, 4, 41, 'OPEN', 'ASSIGNED', 'Duplicate review started', '2026-10-15 14:00:00+05:30'),
  (8, 4, 41, 'ASSIGNED', 'RESOLVED', 'Lift vendor repaired sensor', '2026-10-17 16:00:00+05:30'),
  (9, 5, 10, NULL, 'OPEN', 'Complaint received', '2026-10-20 08:05:00+05:30'),
  (10, 5, 42, 'OPEN', 'ASSIGNED', 'Duplicate linked to complaint 2', '2026-10-20 09:00:00+05:30'),
  (11, 5, 42, 'ASSIGNED', 'RESOLVED', 'Included in vendor visit', '2026-10-21 13:00:00+05:30'),
  (12, 5, 42, 'RESOLVED', 'CLOSED', 'Resident notified', '2026-10-21 16:30:00+05:30'),
  (13, 6, 11, NULL, 'OPEN', 'Complaint received', '2026-10-22 18:25:00+05:30'),
  (14, 6, 41, 'OPEN', 'ASSIGNED', 'Duplicate linked to complaint 2', '2026-10-23 09:00:00+05:30'),
  (15, 6, 41, 'ASSIGNED', 'RESOLVED', 'Lift vibration addressed', '2026-10-24 09:00:00+05:30'),
  (16, 6, 41, 'RESOLVED', 'CLOSED', 'Resident confirmed', '2026-10-24 11:00:00+05:30'),
  (17, 7, 12, NULL, 'OPEN', 'Safety keyword escalated', '2026-11-01 19:45:00+05:30'),
  (18, 7, 42, 'OPEN', 'ASSIGNED', 'Emergency response assigned', '2026-11-01 19:50:00+05:30'),
  (19, 8, 3, NULL, 'OPEN', 'Complaint received', '2026-10-28 14:30:00+05:30'),
  (20, 8, 41, 'OPEN', 'IN_PROGRESS', 'Electrician assigned', '2026-10-28 15:10:00+05:30'),
  (21, 9, 4, NULL, 'OPEN', 'Complaint received', '2026-10-18 10:00:00+05:30'),
  (22, 9, 42, 'OPEN', 'ASSIGNED', 'Plumber assigned', '2026-10-18 11:00:00+05:30'),
  (23, 9, 42, 'ASSIGNED', 'RESOLVED', 'Pressure valve adjusted', '2026-10-19 16:00:00+05:30'),
  (24, 10, 5, NULL, 'OPEN', 'Complaint received', '2026-10-10 20:10:00+05:30'),
  (25, 10, 41, 'OPEN', 'ASSIGNED', 'Parking patrol notified', '2026-10-10 21:00:00+05:30'),
  (26, 10, 41, 'ASSIGNED', 'RESOLVED', 'Vehicle moved', '2026-10-11 08:30:00+05:30'),
  (27, 10, 41, 'RESOLVED', 'CLOSED', 'Resident confirmed', '2026-10-11 09:00:00+05:30'),
  (28, 11, 6, NULL, 'OPEN', 'Security alert raised', '2026-10-30 22:00:00+05:30'),
  (29, 11, 42, 'OPEN', 'ASSIGNED', 'Guard briefing requested', '2026-10-30 22:10:00+05:30'),
  (30, 12, 13, NULL, 'OPEN', 'Complaint received', '2026-11-02 07:25:00+05:30'),
  (31, 13, 14, NULL, 'OPEN', 'Complaint received', '2026-10-25 09:50:00+05:30'),
  (32, 13, 41, 'OPEN', 'IN_PROGRESS', 'Housekeeping supervisor assigned', '2026-10-25 11:00:00+05:30'),
  (33, 14, 15, NULL, 'OPEN', 'Complaint received', '2026-10-14 08:00:00+05:30'),
  (34, 14, 42, 'OPEN', 'ASSIGNED', 'Ramp cleaning assigned', '2026-10-14 09:00:00+05:30'),
  (35, 14, 42, 'ASSIGNED', 'RESOLVED', 'Drain cleared', '2026-10-15 12:00:00+05:30'),
  (36, 15, 16, NULL, 'OPEN', 'Safety alert raised', '2026-10-08 21:30:00+05:30'),
  (37, 15, 41, 'OPEN', 'ASSIGNED', 'Security response dispatched', '2026-10-08 21:35:00+05:30'),
  (38, 15, 41, 'ASSIGNED', 'RESOLVED', 'Gate footage reviewed', '2026-10-08 23:45:00+05:30'),
  (39, 15, 41, 'RESOLVED', 'CLOSED', 'Resident follow-up complete', '2026-10-09 00:15:00+05:30'),
  (40, 16, 17, NULL, 'OPEN', 'Complaint received', '2026-10-31 13:15:00+05:30'),
  (41, 17, 18, NULL, 'OPEN', 'Complaint received', '2026-10-11 16:00:00+05:30'),
  (42, 17, 42, 'OPEN', 'ASSIGNED', 'Waterproofing team assigned', '2026-10-11 17:00:00+05:30'),
  (43, 17, 42, 'ASSIGNED', 'RESOLVED', 'Sealant replaced', '2026-10-13 12:00:00+05:30'),
  (44, 18, 19, NULL, 'OPEN', 'Safety lane alert raised', '2026-11-03 18:10:00+05:30'),
  (45, 18, 41, 'OPEN', 'ASSIGNED', 'Security patrol notified', '2026-11-03 18:20:00+05:30'),
  (46, 19, 20, NULL, 'OPEN', 'Complaint received', '2026-10-19 11:45:00+05:30'),
  (47, 19, 42, 'OPEN', 'ASSIGNED', 'Carpenter assigned', '2026-10-19 13:00:00+05:30'),
  (48, 19, 42, 'ASSIGNED', 'RESOLVED', 'Chair repaired', '2026-10-20 15:00:00+05:30'),
  (49, 20, 21, NULL, 'OPEN', 'Complaint received', '2026-10-27 07:40:00+05:30');

SELECT setval(pg_get_serial_sequence('flat', 'id'), 40);
SELECT setval(pg_get_serial_sequence('app_user', 'id'), 45);
SELECT setval(pg_get_serial_sequence('late_fee_rule', 'id'), 1);
SELECT setval(pg_get_serial_sequence('bill', 'id'), 120);
SELECT setval(pg_get_serial_sequence('payment', 'id'), 113);
SELECT setval(pg_get_serial_sequence('complaint', 'id'), 20);
SELECT setval(pg_get_serial_sequence('complaint_event', 'id'), 49);
