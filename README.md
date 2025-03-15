# ApiModels: ApiTransforms

Transforms ApiObject from and to various formats.  

Formats supported:

- JSON
- XML
- CSV
- YAML
- Fixed Width
- Segmented Fixed Width

# JSON

- JsonObjectParser:  Parser for JSON
- JsonObjectWriter:  Writer for JSON

## Processing

JSON is processed into a single ApiObject, if the input string starts with `[`, then a `root` list is created.

During Writing of JSON, if the ApiObject has ONLY a `root` list, then the JSON will be a list output.

## Example JSON Single Object Usage

If you are dealing with single objects, like in the case of REST endpoints, you can create a Parser object.

```java
JsonObjectParser jsonParser = new JsonObjectParser(true);

ApiObject obj = jsonParser.parseSingle(new StringReader(strInput));
```