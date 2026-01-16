package com.mnu.myblog.domain;

public enum AdminLogType {

    /* ================= 投稿 ================= */
    POST_DELETE("投稿削除"),
    POST_RESTORE("投稿復元"),
    POST_HIDE("投稿非表示"),
    POST_SHOW("投稿表示"),
    POST_PIN("投稿固定"),
    POST_UNPIN("投稿固定解除"),

    /* ================= コメント ================= */
    COMMENT_DELETE("コメント削除"),
    COMMENT_RESTORE("コメント復元"),

    /* ================= 🚨 通報コメント ================= */
    COMMENT_REPORT_SOFT_DELETE("通報コメント非公開処理"),
    COMMENT_REPORT_HARD_DELETE("通報コメント完全削除"),
    COMMENT_REPORT_IGNORE("通報無視"),

    /* ================= ユーザー ================= */
    USER_ROLE_UPDATE("ユーザー権限変更"),
    USER_BAN("会員停止"),
    USER_UNBAN("会員停止解除"),
    USER_WITHDRAW("会員退会"),

    /* ================= お知らせ ================= */
    NOTICE_CREATE("お知らせ作成"),
    NOTICE_DELETE("お知らせ削除"),
    NOTICE_PIN("お知らせ固定"),
    NOTICE_UNPIN("お知らせ固定解除"),
    NOTICE_VISIBLE("お知らせ表示設定変更");

    private final String description;

    AdminLogType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}