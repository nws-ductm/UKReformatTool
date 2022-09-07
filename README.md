**Search conditions for scanner**
- File extension: *.java
- File name ends with: Query, QueryProcessor, QueryProcesser, Finder
- Content contains: `@Stateless`
- Content not contains: `@TransactionAttribute` _(check from start of file to `public class`)_

**Csv reader requirements**
- Csv file format:
  - UK context _(uk.at, uk.com...)_
  - Relative path to file from context (similar to package)  
  - File name
- Content contains: `@Stateless`
- Content not contains: `@TransactionAttribute` _(check from start of file to `public class`)_

**Formatter job**
- Find `@Stateless` line
- Add `\n@TransactionAttribute(TransactionAttributeType.SUPPORTS)\n` after `@Stateless`
- Find `import javax.ejb.Stateless;` line
- Add `\nimport javax.ejb.TransactionAttribute;\nimport javax.ejb.TransactionAttributeType;\n` after `import javax.ejb.Stateless;` line