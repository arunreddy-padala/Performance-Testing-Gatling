package gatlingdemostoreapi;

import java.util.Map;

public class Headers {
  public static final Map<CharSequence, String> authorizationHeader = Map.ofEntries(
          //Use the capture token in api/authentication transaction
          Map.entry("authorization", "Bearer #{jwt}")
  );
}
