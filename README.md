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





ISN,Amount,PCTC,Account,RT,EPC,Serial,DocType,CaptDate,ProcDate,Onus,CreditDebit,TxnLink,FBW File,FBW Offset,FBW Length,BBW File,BBW Offset,BBW Length,Sent/Skip,DepAccount,DepAmount,DocName,WorkType,BatchAcctNo,AppId,Branch,HostTranCode,SiteId,Not Used,DepAcctFlags,Not Used,Not Used,Bank ID,Not Used,Sorter,CostCenter,Not Used,PackageCode,ErrorCode,ErrorMsg,SusInd,AccountId,Analysis Summary,Fraud Type,Total Score,Total Score Threshold,Dep Base Profile,Reprocess Ind,Dep Bank Name,CAR Amount,CAR Score,LAR Amount,LAR Score,MICR Value,MICR Score,Payee Value,Payee Score,Date Value,Date Score,Payer 1,Payer 1 Score,Payer 2,Payer 2 Score,Payer State,Payer State Score,Not Used,Not Used,Not Used,Not Used,Turnaround,Rules Engine Summary,Private Client Data,Dep Profile Name,Dep RT,Item BankID,Item Bank Name,Txn Timestamp,Txn Channel,Dep Place Name,Dep Place Street,Dep Place City,Dep Place State,Dep Place Zip,Txn Operation,Dep Acct Names,Dep Acct Street,Dep Acct City,Dep Acct State,Dep Acct Zip,Onus Base Profile,Known Fraud Profile,Dep Acct Risk Score,Dep Acct Balance,Dep Acct Checks?,Dep Acct Status,Dep Acct Open Date,Not Used,Not Used,Not Used,Not Used,Task Id,Batch ID,Car Lar Discr Score,Car Style Score,Payee Style Score,Lar Style Score,Date Style Score,Amount Str,Amount Discrepancy,Amount Disc Threshold,Amount Out Of Range,Amount Oor Threshold,Amount Over Threshold,Amount Res Score,Amount Result,Amount Threshold For2 Sig,Amount Ver Suspect,Brand New,Check Stock,Check Stock Threshold,Closed Sus,Conditional Ref,Conditional Sus,Dormant Sus,Duplicate Serial Sus,Duplicate Amount Str,Duplicate Doc Date,Duplicate Doc Name,Inactive Sus,Iqua Sus,Match Good Payee,Matching Check Stock,Matching Check Style,Matching Signature,Max Amount,Missing Signature Score,Non Conclusive Sus,Number Of Signatures,Pad Sus,Payee Matching Score,Profile Status,Read Only,Recurring Amount Days,Recurring Amount Occ,Required Num Of Sig,Serial Result,Serial Range Score,Serial Range Threshold,Signature Score,Signature Threshold,Transit Type,Two Sig Sus,Watch Sus,Payee Match Style Score,Counterfeit Final Score,Forgery Final Score,Alteration Final Score,Other Final Score,Alteration Score,Alteration Threshold,Check Writing Style Score,Payee Match Style Item,Camera2 Detection Reliability,Camera2 Diagonal Ratio,Camera2 Relative Field Location,Camera Detection Reliability,Camera Diagonal Ratio,Camera Relative Field Location,Check Armor2 Detection Reliability,Check Armor Detection Reliability,Check Armor Diagonal Ratio,Check Armor Relative Field Location,Frame Micr Position Match,Heat Circle2 Detection Reliability,Heat Circle2 Diagonal Ratio,Heat Circle2 Relative Field Location,Heat Circle Detection Reliability,Heat Circle Diagonal Ratio,Heat Circle Relative Field Location,Padlock Detection Reliability,Padlock Diagonal Ratio,Padlock Relative Field Location,Pay To Detection Reliability,pay To Diagonal Ratio,Pay To Relative Field Location,Payer Block Aspect Ratio,Payer Block Width Ratio,Payer Detection Reliability,CompData.PayerLinesAlignment,Payer Lines Height Consistency,Payer Lines Width Consistency,Payer Max Lines,CompData.PayerNumberOfLines,Payer Pay To Azimuth Diff,Payer Pay To Distance Ratio,Payer Relative Field Location,Payer Relative Field Location Left,Payer Serial Azimuth Diff,Payer Serial Distance Ratio,Shield Detection Reliability,Shield Diagonal Ratio,Shield Relative Field Location,Csv Reasons,Check Dimensions,Frame Micr Relative Location,Payee Aspect Ratio,Payee Dimensions,Payer Aspect Ratio,Payer Dimensions,Payer Line Consistency,SimIndexes.PayerLinesAlignment,SimIndexes.PayerNumberOfLines,Payer Payee Alignment,Relative Field Location,Relative Field Origin Location,Security Symbols Location,Serial Aspect Ratio,Serial Dimensions,Match Bad Payee,Match Bad Payee Score,Match Good Payee Score,Payee Matching Threshold,Velocity Daily Score,Velocity Monthly Score,Velocity Quarterly Score,Velocity Score,Velocity Threshold,Writing Style Reasons
3100463617,156048,,8650163692,500901007,0,0,,20260723,20260723,Y,Y,3100463617,\\vfq00010.fcpd.fcbint.net\Orbograph\orboDeposit_dev\X9\Archive\FCB\20260723\FCB_801007_P_ARCHIVE202607230903.x937,14,0,\\vfq00010.fcpd.fcbint.net\Orbograph\orboDeposit_dev\X9\Archive\FCB\20260723\FCB_801007_P_ARCHIVE202607230903.x937,0,0,Skip,8650163692,156048,3500,1,2153000001,9,,1,1,,,,,,,31,,,,,,,,,,,,,,500901007,,,,,,,,,,,,,,,,,,,,,,,,,500901007,,,,,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,



sample one

Record Type,Auxiliary On Us,External Processing Code,Payor Bank Routing Number,Payor Bank Routing Number Check Digit,On Us,Item Amount,ECE Institution Item Sequence Number,Documentation Type Indicator,Return Acceptance Indicator,MICR Valid Indicator,BFD Indicator,Check Detail Record Addendum Count,Correction Indicator,Archive Type Indicator
25,,,50090100,7,8650163692,156048,3100463617,G,,,,1,,F

