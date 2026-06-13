package com.b2la.dbroffice.api;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class User360ServiceTest {
    @Test
    void filtersActivityWhereUserHasDifferentBusinessRoles() {
        String response = """
                {
                  "data": {
                    "response": [
                      {"id": 1, "idexp": 42, "reference": "sent"},
                      {"id": 2, "ben": {"id": 42}, "reference": "received"},
                      {"id": 3, "idclient": 9, "reference": "other"}
                    ]
                  }
                }
                """;

        var result = JsonParser.parseString(
                User360Service.filterResponse(response, "42", "")).getAsJsonArray();

        assertEquals(2, result.size());
        assertEquals("sent", result.get(0).getAsJsonObject().get("reference").getAsString());
        assertEquals("received", result.get(1).getAsJsonObject().get("reference").getAsString());
    }

    @Test
    void filtersAuditCompatiblePayloadByUsername() {
        String response = """
                [{"username":"+243810000001"},{"username":"+243810000002"}]
                """;

        var result = JsonParser.parseString(
                User360Service.filterResponse(response, "42", "+243810000002")).getAsJsonArray();

        assertEquals(1, result.size());
    }

    @Test
    void filtersSelectedUserBalanceFromAccountsPayload() {
        String response = """
                {
                  "data": {
                    "acctList": [
                      {
                        "acct": {
                          "id": 100,
                          "userId": 42,
                          "balance": {"currency": "USD", "amount": 125.50}
                        }
                      },
                      {
                        "acct": {
                          "id": 101,
                          "userId": 9,
                          "balance": {"currency": "CDF", "amount": 5000}
                        }
                      }
                    ]
                  }
                }
                """;

        var result = JsonParser.parseString(
                User360Service.filterResponse(response, "42", "")).getAsJsonArray();

        assertEquals(1, result.size());
        var account = result.get(0).getAsJsonObject().getAsJsonObject("acct");
        assertEquals(100, account.get("id").getAsInt());
        assertEquals(125.50, account.getAsJsonObject("balance").get("amount").getAsDouble());
    }
}
