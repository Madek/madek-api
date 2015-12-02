package madek.api;

import clojure.lang.ExceptionInfo;
import clojure.lang.IPersistentMap;

public class WebstackException extends ExceptionInfo {
  public WebstackException(String s, IPersistentMap data) {
    super(s, data);
  }
}
