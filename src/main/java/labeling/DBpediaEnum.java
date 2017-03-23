package labeling;

public enum DBpediaEnum {
  // Product
  // Amount
  // Event ���� ��������� ����� ����� �������� dbo:wikiPageDisambiguates
  // Function <http://dbpedia.org/ontology/PersonFunction> - ��� label
  THING("http://nerd.eurecom.fr/ontology#Thing", "T"), // 480�
  PERSON("http://nerd.eurecom.fr/ontology#Person", "P"), // 150�
  LOCATION("http://nerd.eurecom.fr/ontology#Location", "L"), // 130�
  ORGANISATION("http://nerd.eurecom.fr/ontology#Organisation", "O"), // 28�
  ANIMAL("http://nerd.eurecom.fr/ontology#Animal", "A"); // 16�

  private final String type;
  private final String typeChar;

  DBpediaEnum(String type, String typeChar) {
    this.type = type;
    this.typeChar = typeChar;
  }

  public String getType() {
    return type;
  }

  public String getTypeChar() {
    return typeChar;
  }
  
  public static String getTypeCharForType(String type) {
    for(DBpediaEnum val : DBpediaEnum.values()) {
      if(val.getType().equals(type)) {
        return val.getTypeChar();
      }
    }
    return "";
  }

}
