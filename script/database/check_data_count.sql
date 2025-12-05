-- Quick check: How much data in each table?
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'groups', COUNT(*) FROM groups  
UNION ALL
SELECT 'group_members', COUNT(*) FROM group_members
UNION ALL
SELECT 'spam_reports', COUNT(*) FROM spam_reports
UNION ALL
SELECT 'messages', COUNT(*) FROM messages
UNION ALL
SELECT 'group_messages', COUNT(*) FROM group_messages
UNION ALL
SELECT 'login_history', COUNT(*) FROM login_history
UNION ALL
SELECT 'friends', COUNT(*) FROM friends
ORDER BY table_name;
