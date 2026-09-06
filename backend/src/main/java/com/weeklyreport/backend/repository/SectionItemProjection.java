package com.weeklyreport.backend.repository;

/** One blocker or achievement plus the member it belongs to, for the section-comparison view. */
public interface SectionItemProjection {

    Long getUserId();

    String getDescription();

    boolean isKeyItem();
}
