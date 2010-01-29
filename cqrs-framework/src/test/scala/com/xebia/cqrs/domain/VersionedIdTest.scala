package com.xebia.cqrs.domain;

import org.junit.Assert._;

import java.util.UUID;

import org.junit.Test;


class VersionedIdTest {
    
    val FOO = UUID.randomUUID();
    val BAR = UUID.randomUUID();
    
    val a = VersionedId.forSpecificVersion(FOO, 3L);
    
    @Test
    def shouldStoreIdAndVersion() {
        assertEquals(FOO, a.id);
        assertEquals(3L, a.version);
    }
    
    @Test
    def testEqualsIgnoreVersion() {
        assertTrue(a.equalsIgnoreVersion(VersionedId.forSpecificVersion(FOO, 1)));
        assertFalse(a.equalsIgnoreVersion(null));
        assertFalse(a.equalsIgnoreVersion(VersionedId.forSpecificVersion(BAR, 3)));
    }
    
    @Test
    def testCompatibility() {
        assertTrue(VersionedId.forLatestVersion(FOO).isCompatible(a));
        assertFalse(VersionedId.forLatestVersion(BAR).isCompatible(a));
        assertTrue(a.isCompatible(VersionedId.forSpecificVersion(FOO, 3)));
        assertFalse(a.isCompatible(VersionedId.forSpecificVersion(FOO, 2)));
        assertFalse(a.isCompatible(VersionedId.forSpecificVersion(BAR, 3)));
    }

}
