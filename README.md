**Search conditions for com.uk.reformattool.scanner**

- File extension: *.java
- File name ends with: Query, QueryProcessor, QueryProcesser, Finder

**Csv reader requirements**

- Csv file format:
    - UK context _(uk.at, uk.com...)_
    - Relative path to file from context (a/b/c)
    - File name

**File content conditions**

- Content contains: `@Stateless`
- Content not contains: `@TransactionAttribute` _(check from start of file to `public class`)_

**Formatter job**

- Find `@Stateless` line
- Add `\n@TransactionAttribute(TransactionAttributeType.SUPPORTS)\n` after `@Stateless`
- Find `import javax.ejb.Stateless;` line
- Add `\nimport javax.ejb.TransactionAttribute;\nimport javax.ejb.TransactionAttributeType;\n`
  after `import javax.ejb.Stateless;` line