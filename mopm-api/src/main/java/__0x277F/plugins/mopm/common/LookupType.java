package __0x277F.plugins.mopm.common;

/**
 * Things that can be looked up on a worker thread.
 */
public enum LookupType {
    /**
     * 'A' record lookup. Normal type for DNSBLs
     */
    A,
    /**
     * Used by the plugin's internal auto-updater
     */
    UPDATE,
}