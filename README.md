# X9 Parser

A Spring Boot tool for reading X9.37 (DSTU) check files. You give it the path to
an X9 file; it streams through the file, writes the check data to two CSV files
and the check images to a folder, and shows where everything was saved.

It reads one record at a time instead of loading the whole file, so it handles
large files (thousands of checks, GB-sized) without running out of memory.

## What it does

- Takes a file path (a full path, or a file name looked for in a configured folder).
- Writes two CSVs: a short one with the check-detail fields, and the big
  Orbograph-format one (filling the columns X9 provides, leaving the rest blank).
- Writes each check's front and rear image as a TIF.
- Shows a summary: checks parsed, rows written, images written, and the location.

## Tech stack

- Java 21
- Spring Boot 4.1 (Spring Web MVC, Thymeleaf)
- Maven

## Running it

Requires Java 21.

```
./mvnw spring-boot:run
```

Open http://localhost:8080, enter the path to an X9 file, and click **Parse file**.
Output goes to the folder set by `x9.output-dir` (default `./output`).

Run the tests:

```
./mvnw test
```

## How the X9 parsing works

- Records are length-prefixed: a 4-byte length, then that many bytes, repeated.
- Text fields are EBCDIC (`Cp1047`); images are raw TIF bytes.
- Record types: `01` file header, `25` check detail (the MICR fields), and `52`
  image data (the first image in a check is the front, the second the rear).

## Testing

- `X9ConfigTest` - the settings load from the properties file.
- `X9StreamProcessorTest` - the sample file produces 10 checks, 20 images, and both CSVs.
- `ParseFlowTest` - the web flow: form loads, a good file shows a summary, bad input shows a message.

## Authentication

Authentication and authorization are provided by Active Directory, running on
a Windows Server EC2 instance. The application authenticates via LDAP bind
against the domain `fcrm.local` and derives roles from AD group membership
(members of `FCRMADMIN` can access the parse function).

Running the app and its tests requires the AD server to be reachable. Because
the security and Selenium tests authenticate against a live network directory
rather than in-memory, they are occasionally slower or less
deterministic than the rest of the suite (this is expected for tests against external infrastructure).

## Output CSV formats

Two files are written each run. The big format matches the downstream Orbograph
layout; the short format is the raw check-detail fields.

Big format:

```
ISN,Amount,PCTC,Account,RT,EPC,Serial,DocType,CaptDate,ProcDate,Onus,CreditDebit,TxnLink,FBW File,FBW Offset,FBW Length,BBW File,BBW Offset,BBW Length,Sent/Skip,DepAccount,DepAmount,DocName,WorkType,BatchAcctNo,AppId,Branch,HostTranCode,SiteId,Not Used,DepAcctFlags,Not Used,Not Used,Bank ID,Not Used,Sorter,CostCenter,Not Used,PackageCode,ErrorCode,ErrorMsg,SusInd,AccountId,Analysis Summary,Fraud Type,Total Score,Total Score Threshold,Dep Base Profile,Reprocess Ind,Dep Bank Name,CAR Amount,CAR Score,LAR Amount,LAR Score,MICR Value,MICR Score,Payee Value,Payee Score,Date Value,Date Score,Payer 1,Payer 1 Score,Payer 2,Payer 2 Score,Payer State,Payer State Score,Not Used,Not Used,Not Used,Not Used,Turnaround,Rules Engine Summary,Private Client Data,Dep Profile Name,Dep RT,Item BankID,Item Bank Name,Txn Timestamp,Txn Channel,Dep Place Name,Dep Place Street,Dep Place City,Dep Place State,Dep Place Zip,Txn Operation,Dep Acct Names,Dep Acct Street,Dep Acct City,Dep Acct State,Dep Acct Zip,Onus Base Profile,Known Fraud Profile,Dep Acct Risk Score,Dep Acct Balance,Dep Acct Checks?,Dep Acct Status,Dep Acct Open Date,Not Used,Not Used,Not Used,Not Used,Task Id,Batch ID,Car Lar Discr Score,Car Style Score,Payee Style Score,Lar Style Score,Date Style Score,Amount Str,Amount Discrepancy,Amount Disc Threshold,Amount Out Of Range,Amount Oor Threshold,Amount Over Threshold,Amount Res Score,Amount Result,Amount Threshold For2 Sig,Amount Ver Suspect,Brand New,Check Stock,Check Stock Threshold,Closed Sus,Conditional Ref,Conditional Sus,Dormant Sus,Duplicate Serial Sus,Duplicate Amount Str,Duplicate Doc Date,Duplicate Doc Name,Inactive Sus,Iqua Sus,Match Good Payee,Matching Check Stock,Matching Check Style,Matching Signature,Max Amount,Missing Signature Score,Non Conclusive Sus,Number Of Signatures,Pad Sus,Payee Matching Score,Profile Status,Read Only,Recurring Amount Days,Recurring Amount Occ,Required Num Of Sig,Serial Result,Serial Range Score,Serial Range Threshold,Signature Score,Signature Threshold,Transit Type,Two Sig Sus,Watch Sus,Payee Match Style Score,Counterfeit Final Score,Forgery Final Score,Alteration Final Score,Other Final Score,Alteration Score,Alteration Threshold,Check Writing Style Score,Payee Match Style Item,Camera2 Detection Reliability,Camera2 Diagonal Ratio,Camera2 Relative Field Location,Camera Detection Reliability,Camera Diagonal Ratio,Camera Relative Field Location,Check Armor2 Detection Reliability,Check Armor Detection Reliability,Check Armor Diagonal Ratio,Check Armor Relative Field Location,Frame Micr Position Match,Heat Circle2 Detection Reliability,Heat Circle2 Diagonal Ratio,Heat Circle2 Relative Field Location,Heat Circle Detection Reliability,Heat Circle Diagonal Ratio,Heat Circle Relative Field Location,Padlock Detection Reliability,Padlock Diagonal Ratio,Padlock Relative Field Location,Pay To Detection Reliability,pay To Diagonal Ratio,Pay To Relative Field Location,Payer Block Aspect Ratio,Payer Block Width Ratio,Payer Detection Reliability,CompData.PayerLinesAlignment,Payer Lines Height Consistency,Payer Lines Width Consistency,Payer Max Lines,CompData.PayerNumberOfLines,Payer Pay To Azimuth Diff,Payer Pay To Distance Ratio,Payer Relative Field Location,Payer Relative Field Location Left,Payer Serial Azimuth Diff,Payer Serial Distance Ratio,Shield Detection Reliability,Shield Diagonal Ratio,Shield Relative Field Location,Csv Reasons,Check Dimensions,Frame Micr Relative Location,Payee Aspect Ratio,Payee Dimensions,Payer Aspect Ratio,Payer Dimensions,Payer Line Consistency,SimIndexes.PayerLinesAlignment,SimIndexes.PayerNumberOfLines,Payer Payee Alignment,Relative Field Location,Relative Field Origin Location,Security Symbols Location,Serial Aspect Ratio,Serial Dimensions,Match Bad Payee,Match Bad Payee Score,Match Good Payee Score,Payee Matching Threshold,Velocity Daily Score,Velocity Monthly Score,Velocity Quarterly Score,Velocity Score,Velocity Threshold,Writing Style Reasons
```

Short format:

```
Record Type,Auxiliary On Us,External Processing Code,Payor Bank Routing Number,Payor Bank Routing Number Check Digit,On Us,Item Amount,ECE Institution Item Sequence Number,Documentation Type Indicator,Return Acceptance Indicator,MICR Valid Indicator,BFD Indicator,Check Detail Record Addendum Count,Correction Indicator,Archive Type Indicator
25,,,50090100,7,8650163692,156048,3100463617,G,,,,1,,F
```

## Limitations

Built for standard forward-presentment X9.37 files (a front and rear image per
check). The Orbograph columns an X9 file can't supply (fraud scores, capture
metadata) are left blank.