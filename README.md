# Slice

[![CI](https://github.com/hwesselmann/slice/actions/workflows/ci.yml/badge.svg)](https://github.com/hwesselmann/slice/actions/workflows/ci.yml)

Standalone Java library that parses official **DTB (Deutscher Tennis Bund) ranking PDFs** into typed, validated Java models.

Supported disciplines: **Herren · Damen · Junioren · Juniorinnen**

## Design principle

> *Lieber laut scheitern als still falsche Daten liefern.*
> (Fail loud rather than silently deliver wrong data.)

Every line that cannot be parsed or validated becomes a `ParseIssue` — never silently-wrong data.

## Requirements

- Java 25
- Maven (the `mvnw` wrapper is included)

## Dependency

Release artifacts are published to Maven Central. SNAPSHOT builds (from `main`) are available via GitHub Packages.

```xml
<dependency>
    <groupId>de.hdawg.slice</groupId>
    <artifactId>slice</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

For SNAPSHOT builds, add the GitHub Packages repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/hwesselmann/slice</url>
    </repository>
</repositories>
```

## Build

```bash
./mvnw clean package
```

Run the test suite:

```bash
./mvnw test
```

## Usage

```java
import de.hdawg.slice.Slice;
import de.hdawg.slice.api.ParseResult;
import de.hdawg.slice.api.RankingList;
import de.hdawg.slice.api.RankingEntry;
import de.hdawg.slice.api.Score;
import de.hdawg.slice.validate.ParseMode;
import java.nio.file.Path;

// LENIENT (default): unparseable rows are recorded as ParseIssues and skipped
Slice slice = Slice.builder().build();

// STRICT: throws IllegalStateException on the first ERROR-severity issue
Slice strict = Slice.builder().mode(ParseMode.STRICT).build();

ParseResult result = slice.parse(Path.of("DTB-Herren-Rangliste_20260101.pdf"));

RankingList ranking = result.list();
System.out.println(ranking.discipline());        // HERREN
System.out.println(ranking.stichtag());          // 2025-12-31
System.out.println(ranking.validFrom());         // 2026-01-09
System.out.println(ranking.pointsThreshold());   // 603  (null for juniors)
System.out.println(ranking.birthYears());        // null (YearRange for juniors)

for (RankingEntry entry : ranking.entries()) {
    System.out.printf("%d  %-20s %-15s  %s  %s  %s  %s%n",
        entry.rank(),
        entry.lastName(),
        entry.firstName(),
        entry.dtbId(),
        entry.association(),
        entry.club(),
        entry.score()
    );
}

// Review any issues (WARNING or ERROR)
result.issues().forEach(issue ->
    System.err.printf("[%s] page %d — %s%n", issue.severity(), issue.page(), issue.message())
);
```

## API

### `Slice`

| Method | Description |
|---|---|
| `Slice.builder()` | Returns a `Builder` |
| `builder.mode(ParseMode)` | `LENIENT` (default) or `STRICT` |
| `slice.parse(Path pdf)` | Parses the PDF; returns `ParseResult` |

### `ParseResult`

| Field | Type | Description |
|---|---|---|
| `list()` | `RankingList` | The parsed ranking |
| `issues()` | `List<ParseIssue>` | All warnings and errors encountered |

### `RankingList`

| Field | Type | Description |
|---|---|---|
| `discipline()` | `Discipline` | `HERREN`, `DAMEN`, `JUNIOREN`, `JUNIORINNEN` |
| `stichtag()` | `LocalDate` | Reference date of the ranking (required; `LocalDate.MIN` if missing and LENIENT) |
| `validFrom()` | `LocalDate` | Validity date; `LocalDate.MIN` if absent |
| `pointsThreshold()` | `@Nullable Integer` | Points cutoff (Herren/Damen only; `null` for juniors) |
| `birthYears()` | `@Nullable YearRange` | Birth-year range (juniors only; `null` for Herren/Damen) |
| `entries()` | `List<RankingEntry>` | Ranked entries, in ranking order |

### `RankingEntry`

| Field | Type | Description |
|---|---|---|
| `rank()` | `int` | Rank (1-based) |
| `lastName()` | `String` | Last name |
| `firstName()` | `String` | First name |
| `nationality()` | `@Nullable String` | IOC/DOSB three-letter code (e.g. `GER`, `SUI`) |
| `dtbId()` | `String` | 8-digit DTB player ID |
| `association()` | `String` | Regional tennis association (VBD) abbreviation |
| `club()` | `String` | Club name |
| `score()` | `Score` | See `Score` variants below |

### `Score`

A sealed interface with four implementations:

| Type | Source PDF token | Meaning |
|---|---|---|
| `Score.Points(BigDecimal value)` | e.g. `1234,5` | Numeric DTB points |
| `Score.International.ATP` | *(no token — senior Herren without DTB points)* | Ranked via ATP |
| `Score.International.WTA` | *(no token — senior Damen without DTB points)* | Ranked via WTA |
| `Score.ProtectedRanking.INSTANCE` | `PR` | Protected ranking (keeps position without current points) |
| `Score.Projected.INSTANCE` | `Einst.` | Projected entry (placed despite insufficient wins) |

The number of seniors at the top of a Herren/Damen list who carry `International` scores instead of DTB points varies each year and is not validated against a fixed threshold.

Use a pattern-matching switch to handle all cases:

```java
String display = switch (entry.score()) {
    case Score.Points p          -> p.value().toPlainString();
    case Score.International i   -> i.name();           // "ATP" or "WTA"
    case Score.ProtectedRanking r -> "PR";
    case Score.Projected p       -> "Einst.";
};
```

### `ParseIssue`

| Field | Type | Description |
|---|---|---|
| `severity()` | `Severity` | `WARNING` or `ERROR` |
| `page()` | `int` | PDF page number (1-based) |
| `rawLine()` | `String` | The raw text of the offending line |
| `message()` | `String` | Human-readable description (German) |

### Parse modes

| Mode | ERROR issue | WARNING issue |
|---|---|---|
| `LENIENT` | Row/field skipped; issue recorded | Issue recorded; entry included |
| `STRICT` | `IllegalStateException` thrown immediately | Issue recorded; entry included |

## Nullability

All packages are `@NullMarked` (JSpecify). Fields that may be absent are annotated `@Nullable` — there are no sentinel values.
