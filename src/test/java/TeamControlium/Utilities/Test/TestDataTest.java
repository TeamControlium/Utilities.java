package TeamControlium.Utilities.Test;

import TeamControlium.Utilities.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class TestDataTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void Gash() throws Exception {

        String dataSubmitted = "item1 data";
        String category = "Testcat";
        String itemName = "Item1";

        TestData.setItem(category,itemName,dataSubmitted);

        String dataRetrieved = TestData.getItem(String.class,category,itemName);

        assertEquals(dataSubmitted,dataRetrieved,String.format("Verify Test data <%s>, submitted to Category <%s>, Key <%s> is retreived correctly.",dataSubmitted,category,itemName));
    }
}