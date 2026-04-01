SET NAMES utf8mb4;
USE novaleap;

ALTER TABLE notes
    ADD COLUMN reject_reason VARCHAR(240) NULL COMMENT '审核失败原因（展示给用户）';

ALTER TABLE notes
    ADD COLUMN audit_source VARCHAR(32) NULL COMMENT '审核来源：AI/MANUAL/RULES';

ALTER TABLE notes
    ADD COLUMN audited_at DATETIME NULL COMMENT '审核时间';
