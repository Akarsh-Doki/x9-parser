# X9 Parser

A Spring Boot web application for reading X9.37 (DSTU) check files. Upload a
file and it extracts the check data into a table you can download as a CSV, and
pulls each check's front and rear image out as a downloadable TIF.

- Upload an X9.37 file through the browser.
- Parse the Check Detail records into a table of check fields.
- Extract the front and rear image of each check.
- Download the check data as a CSV, and each image as a TIF.

## Tech stack

- Java 21
- Spring Boot 4.1 (Spring Web MVC, Thymeleaf, Validation)
- Maven

## Running it

Requires Java 21.

```
./mvnw spring-boot:run
```

Then open http://localhost:8080, choose an X9 file, and click **Parse file**.

Run the tests:

```
./mvnw test
```

## How the X9 parsing works

X9.37 is the ANSI standard behind Check 21 "image cash letter" files - the
format banks use to exchange scanned checks and their data. A few things make
it non-trivial:

- **Record framing.** The file is a sequence of records. Each record is preceded
  by a 4-byte big-endian length: read the length, then that many bytes, and
  repeat until the file ends.
- **EBCDIC.** The text fields are encoded in EBCDIC (Java charset `Cp1047`), not
  ASCII, so each record's bytes are decoded to text before fields are read.
- **Record types.** The first two characters of a record identify its type. This
  app uses three:
  - `01` File Header - every valid X9 file starts with one (used as a sanity check).
  - `25` Check Detail - the check's MICR fields (routing number, amount, on-us,
    sequence number, and so on), read from fixed positions.
  - `52` Image View Data - holds one embedded TIF image. Three variable-length
    pieces come before the image (an image reference key, a digital signature,
    and the image data), each with its own length field, so the image's start
    and length are computed from those before the raw TIF bytes are copied out.
- **Images per check.** Within a check the first image record is the front and
  the second is the rear. Each Check Detail record starts a new check, so images
  stay tied to the check they follow.

## Project layout

```
controller/   web layer: upload, results page, downloads, error handling
service/      parsing (X9ParserService) and CSV output (X9CsvWriter)
model/        typed data: CheckRecord, CheckImage, ParseResult
exception/    X9ParseException
resources/templates   Thymeleaf pages (upload, result, error)
resources/static/css  stylesheet
```
## Testing

16 tests:

- `X9ParserServiceTest` and `X9CsvWriterTest` - pure unit tests, no Spring, run
  in milliseconds (possible because the service works on plain bytes).
- `UploadFlowTest` - the whole flow through the web layer: upload, results page,
  CSV and image downloads, and the main error paths.

## Limitations

Built and verified against standard forward-presentment X9.37 files (a front and
a rear image per check, in order). Returns files (ICLR) or files with a
different image mix would need the image side and count read from the
image-detail / addendum records rather than inferred by position.
