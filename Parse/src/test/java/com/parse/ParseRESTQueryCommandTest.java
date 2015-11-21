/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse;

import com.parse.http.ParseHttpRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class ParseRESTQueryCommandTest {

  //region testEncode

  @Test
  public void testEncodeWithNoCount() throws Exception {
    ParseQuery.State<ParseObject> state = new ParseQuery.State.Builder<>("TestObject")
        .orderByAscending("orderKey")
        .addCondition("inKey", "$in", Arrays.asList("inValue", "inValueAgain"))
        .selectKeys(Arrays.asList("selectedKey, selectedKeyAgain"))
        .include("includeKey")
        .setLimit(5)
        .setSkip(6)
        .redirectClassNameForKey("extraKey")
        .setTracingEnabled(true)
        .build();

    Map<String, String> encoded = ParseRESTQueryCommand.encode(state, false);

    assertEquals("orderKey", encoded.get("order"));
    JSONObject conditionJson = new JSONObject(encoded.get("where"));
    JSONArray conditionWhereJsonArray = new JSONArray()
        .put("inValue")
        .put("inValueAgain");
    assertEquals(
        conditionWhereJsonArray,
        conditionJson.getJSONObject("inKey").getJSONArray("$in"),
        JSONCompareMode.NON_EXTENSIBLE);
    assertTrue(encoded.get("keys").contains("selectedKey"));
    assertTrue(encoded.get("keys").contains("selectedKeyAgain"));
    assertEquals("includeKey", encoded.get("include"));
    assertEquals("5", encoded.get("limit"));
    assertEquals("6", encoded.get("skip"));
    assertEquals("extraKey", encoded.get("redirectClassNameForKey"));
    assertEquals("1", encoded.get("trace"));
  }

  @Test
  public void testEncodeWithCount() throws Exception {
    ParseQuery.State<ParseObject> state = new ParseQuery.State.Builder<>("TestObject")
        .setLimit(5)
        .setSkip(6)
        .build();

    Map<String, String> encoded = ParseRESTQueryCommand.encode(state, true);

    assertFalse(encoded.containsKey("limit"));
    assertFalse(encoded.containsKey("skip"));
    assertEquals("1", encoded.get("count"));
  }

  //endregion

  //region testConstruct

  @Test
  public void testFindCommand() throws Exception {
    ParseQuery.State<ParseObject> state = new ParseQuery.State.Builder<>("TestObject")
        .selectKeys(Arrays.asList("key", "kayAgain"))
        .build();

    ParseRESTQueryCommand command = ParseRESTQueryCommand.findCommand(state, "sessionToken");

    assertEquals("classes/TestObject", command.httpPath);
    assertEquals(ParseHttpRequest.Method.GET, command.method);
    assertEquals("sessionToken", command.getSessionToken());
    Map<String, String> parameters = ParseRESTQueryCommand.encode(state, false);
    JSONObject jsonParameters = (JSONObject) NoObjectsEncoder.get().encode(parameters);
    assertEquals(jsonParameters, command.jsonParameters, JSONCompareMode.NON_EXTENSIBLE);
  }

  @Test
  public void testCountCommand() throws Exception {
    ParseQuery.State<ParseObject> state = new ParseQuery.State.Builder<>("TestObject")
        .selectKeys(Arrays.asList("key", "kayAgain"))
        .build();

    ParseRESTQueryCommand command = ParseRESTQueryCommand.countCommand(state, "sessionToken");

    assertEquals("classes/TestObject", command.httpPath);
    assertEquals(ParseHttpRequest.Method.GET, command.method);
    assertEquals("sessionToken", command.getSessionToken());
    Map<String, String> parameters = ParseRESTQueryCommand.encode(state, true);
    JSONObject jsonParameters = (JSONObject) NoObjectsEncoder.get().encode(parameters);
    assertEquals(jsonParameters, command.jsonParameters, JSONCompareMode.NON_EXTENSIBLE);
  }

  //endregion
}
