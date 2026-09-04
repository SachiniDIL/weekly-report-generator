-- Soft-delete state for established (ACTIVE) users whose row must survive because
-- later batches (Reports, ProjectAssignments) will reference it by foreign key.
ALTER TYPE user_status ADD VALUE 'REMOVED';
