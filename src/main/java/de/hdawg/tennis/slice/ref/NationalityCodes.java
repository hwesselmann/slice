package de.hdawg.tennis.slice.ref;

import java.util.Set;

/**
 * Whitelist of nationality codes as they are actually printed on official DTB tennis ranking PDFs.
 * These are the <strong>IOC/DOSB</strong> three-letter codes (e.g. {@code GER} for Germany, {@code
 * SUI} for Switzerland, {@code NED} for the Netherlands), <em>not</em> the ISO-3166-1 alpha-3 codes
 * — the canonical example being {@code GER} (DTB) versus {@code DEU} (ISO).
 *
 * <p>The parser captures any 3-letter uppercase token in the nationality slot verbatim; this
 * whitelist is only consulted by the validator. A code that occurs in real DTB data must therefore
 * be listed here so it validates cleanly, while a genuinely-unknown future code fails loud (ERROR)
 * rather than being silently accepted — honouring the prime directive "lieber laut scheitern als
 * still falsche Daten liefern".
 *
 * <p>The IOC/DOSB codes below were derived empirically from the four fixture DTB ranking PDFs
 * (herren, damen, junioren, juniorinnen). Legacy ISO-3166-1 alpha-3 codes are retained as harmless
 * extras.
 */
public final class NationalityCodes {

  /**
   * IOC/DOSB nationality codes that differ from the ISO-3166-1 alpha-3 code and were observed
   * empirically in the four fixture DTB ranking PDFs (scanned in LENIENT mode; see {@code
   * NatCodeScanTest} history). All other codes occurring in the fixtures (e.g. AUT, ESP, TUR, UKR,
   * SRB, SVK, ROU) happen to coincide with their ISO code and are covered by {@link #ISO_CODES}
   * below.
   *
   * <p>Kept deliberately to the empirically-observed set: inventing extra "plausible" IOC codes
   * would let unverified codes pass validation silently and weaken the fail-loud guarantee. A new,
   * genuinely-unknown code must surface as an ERROR rather than be quietly accepted.
   */
  private static final Set<String> IOC_CODES =
      Set.of(
          "GER", // Germany (ISO: DEU) — canonical IOC-vs-ISO example
          "SUI", // Switzerland (ISO: CHE)
          "NED", // Netherlands (ISO: NLD)
          "CRO", // Croatia (ISO: HRV)
          "BUL", // Bulgaria (ISO: BGR)
          "LAT", // Latvia (ISO: LVA)
          "IRI", // Iran (ISO: IRN)
          "RSA", // South Africa (ISO: ZAF)
          "GRE", // Greece (ISO: GRC)
          "SLO" // Slovenia (ISO: SVN)
          );

  /** Legacy ISO-3166-1 alpha-3 codes, retained as harmless extras. */
  private static final Set<String> ISO_CODES =
      Set.of(
          "ABW", "AFG", "AGO", "AIA", "ALA", "ALB", "AND", "ARE", "ARG", "ARM", "ASM", "ATA", "ATF",
          "ATG", "AUS", "AUT", "AZE", "BDI", "BEL", "BEN", "BES", "BFA", "BGD", "BGR", "BHR", "BHS",
          "BIH", "BLM", "BLR", "BLZ", "BMU", "BOL", "BRA", "BRB", "BRN", "BTN", "BVT", "BWA", "CAF",
          "CAN", "CCK", "CHE", "CHL", "CHN", "CIV", "CMR", "COD", "COG", "COK", "COL", "COM", "CPV",
          "CRI", "CUB", "CUW", "CXR", "CYM", "CYP", "CZE", "DEU", "DJI", "DMA", "DNK", "DOM", "DZA",
          "ECU", "EGY", "ERI", "ESH", "ESP", "EST", "ETH", "FIN", "FJI", "FLK", "FRA", "FRO", "FSM",
          "GAB", "GBR", "GEO", "GGY", "GHA", "GIB", "GIN", "GLP", "GMB", "GNB", "GNQ", "GRC", "GRD",
          "GRL", "GTM", "GUF", "GUM", "GUY", "HKG", "HMD", "HND", "HRV", "HTI", "HUN", "IDN", "IMN",
          "IND", "IOT", "IRL", "IRN", "IRQ", "ISL", "ISR", "ITA", "JAM", "JEY", "JOR", "JPN", "KAZ",
          "KEN", "KGZ", "KHM", "KIR", "KNA", "KOR", "KWT", "LAO", "LBN", "LBR", "LBY", "LCA", "LIE",
          "LKA", "LSO", "LTU", "LUX", "LVA", "MAC", "MAF", "MAR", "MCO", "MDA", "MDG", "MDV", "MEX",
          "MHL", "MKD", "MLI", "MLT", "MMR", "MNE", "MNG", "MNP", "MOZ", "MRT", "MSR", "MTQ", "MUS",
          "MWI", "MYS", "MYT", "NAM", "NCL", "NER", "NFK", "NGA", "NIC", "NIU", "NLD", "NOR", "NPL",
          "NRU", "NZL", "OMN", "PAK", "PAN", "PCN", "PER", "PHL", "PLW", "PNG", "POL", "PRI", "PRK",
          "PRT", "PRY", "PSE", "PYF", "QAT", "REU", "ROU", "RUS", "RWA", "SAU", "SDN", "SEN", "SGP",
          "SGS", "SHN", "SJM", "SLB", "SLE", "SLV", "SMR", "SOM", "SPM", "SRB", "SSD", "STP", "SUR",
          "SVK", "SVN", "SWE", "SWZ", "SXM", "SYC", "SYR", "TCA", "TCD", "TGO", "THA", "TJK", "TKL",
          "TKM", "TLS", "TON", "TTO", "TUN", "TUR", "TUV", "TWN", "TZA", "UGA", "UKR", "UMI", "URY",
          "USA", "UZB", "VAT", "VCT", "VEN", "VGB", "VIR", "VNM", "VUT", "WLF", "WSM", "YEM", "ZAF",
          "ZMB", "ZWE");

  private NationalityCodes() {}

  /**
   * Returns {@code true} if {@code code} is a recognised nationality code — either an IOC/DOSB code
   * observed in DTB ranking fixtures or a legacy ISO-3166-1 alpha-3 code.
   *
   * @param code a three-letter uppercase string to look up
   */
  public static boolean isKnown(String code) {
    return IOC_CODES.contains(code) || ISO_CODES.contains(code);
  }
}
