-- Demo data for ScheduleIt screenshots
-- Resets and seeds a realistic week (Mon-Fri visible)

DELETE FROM template_event_override;
DELETE FROM day_event;
DELETE FROM event;
DELETE FROM day_assignment;
DELETE FROM day_template;

UPDATE schedule_settings SET start_minute = 480, end_minute = 1320, notifications_enabled = 1, onboarding_completed = 1 WHERE id = 1;

-- Templates (one per visible day)
INSERT INTO day_template(id, name) VALUES (1, '');
INSERT INTO day_template(id, name) VALUES (2, '');
INSERT INTO day_template(id, name) VALUES (3, '');
INSERT INTO day_template(id, name) VALUES (4, '');
INSERT INTO day_template(id, name) VALUES (5, '');

-- Day assignments (ISO: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri)
INSERT INTO day_assignment(day, template_id) VALUES (1, 1);
INSERT INTO day_assignment(day, template_id) VALUES (2, 2);
INSERT INTO day_assignment(day, template_id) VALUES (3, 3);
INSERT INTO day_assignment(day, template_id) VALUES (4, 4);
INSERT INTO day_assignment(day, template_id) VALUES (5, 5);

-- Colors (ARGB as signed long, but values fit in unsigned 32-bit so positive):
-- Blue 0xFF42A5F5 = 4282682869
-- Green 0xFF66BB6A = 4285244778
-- Orange 0xFFFFA726 = 4294947110
-- Purple 0xFFAB47BC = 4289596860
-- Red 0xFFEF5350 = 4293218128
-- Teal 0xFF26A69A = 4280391322
-- Amber 0xFFFFCA28 = 4294953000
-- Indigo 0xFF5C6BC0 = 4284245440
-- Pink 0xFFEC407A = 4293218170

-- Monday
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (1, 'Morning workout', 480, 540, 4294947110, '5k run + stretch');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (1, 'Deep work', 570, 720, 4282682869, 'Auth refactor');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (1, 'Lunch with Sarah', 750, 810, 4285244778, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (1, 'Client meeting', 840, 960, 4289596860, 'Q2 roadmap review');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (1, 'Dentist', 1050, 1140, 4293218128, '');

-- Tuesday
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (2, 'Standup', 540, 570, 4284245440, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (2, 'Code review', 570, 750, 4282682869, 'Backend PRs');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (2, 'Lunch', 780, 840, 4285244778, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (2, 'Project planning', 840, 990, 4280391322, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (2, 'Yoga class', 1080, 1170, 4289596860, 'Studio downtown');

-- Wednesday
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (3, 'Coffee + emails', 510, 570, 4294953000, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (3, 'Sprint review', 600, 720, 4282682869, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (3, 'Team lunch', 750, 810, 4285244778, 'New ramen place');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (3, 'Doctor appointment', 870, 930, 4293218128, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (3, 'Pair programming', 960, 1080, 4280391322, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (3, 'Family dinner', 1140, 1260, 4293218170, '');

-- Thursday
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (4, 'Morning run', 480, 540, 4294947110, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (4, 'Customer calls', 570, 660, 4284245440, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (4, 'Coffee with Alex', 690, 750, 4294953000, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (4, 'Lunch break', 780, 870, 4285244778, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (4, 'Deep work', 870, 1020, 4282682869, 'Ship v2.1');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (4, 'Climbing gym', 1110, 1230, 4289596860, '');

-- Friday
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (5, 'Weekly review', 540, 600, 4284245440, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (5, 'Demo day', 630, 720, 4289596860, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (5, 'Long lunch', 750, 840, 4285244778, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (5, 'Wrap up + writing', 870, 990, 4282682869, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (5, 'Beers with team', 1020, 1110, 4294953000, '');
INSERT INTO event(template_id, title, start_minute, end_minute, color, notes) VALUES (5, 'Date night', 1170, 1260, 4293218170, '');
