package umm3601.item;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;
import java.util.Map;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import java.security.NoSuchAlgorithmException;

public class ItemController {
  static final String ITEM_TYPE_KEY = "itemType";
  static final String FOOD_TYPE_KEY = "foodType";
  static final String SORT_ORDER_KEY = "sortorder";
  private static final String ITEM_NAME_REGEX = "^(/w+)$";


  private final JacksonMongoCollection<Item> itemCollection;

  public ItemController(MongoDatabase database) {
    itemCollection = JacksonMongoCollection.builder().build(
      database,
      "Item",
      Item.class,
      UuidRepresentation.STANDARD);
  }
  /**
   * Set the JSON body of the response to be the single Item
   * specified by the `id` parameter in the Item
   *
   * @param ctx a Javalin HTTP context
   */
  public void getItem(Context ctx) {
    String id = ctx.pathParam("id");
    Item item;

    try {
      item = itemCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The desired Item id wasn't a legal Mongo Object ID.");
    }
    if (item == null) {
      throw new NotFoundResponse("The desired Item was not found");
    } else {
      ctx.json(item);
      ctx.status(HttpStatus.OK);
    }
  }

  /**
   * Set the JSON body of the response to be a list of all the items returned from the database
   * that match any itemed filters and ordering
   *
   * @param ctx a Javalin HTTP context
   */
  public void getItems(Context ctx) {
    Bson combinedFilter = constructFilter(ctx);
    Bson sortingOrder = constructSortingOrder(ctx);

    // All three of the find, sort, and into steps happen "in parallel" inside the
    // database system. So MongoDB is going to find the items with the specified
    // properties, return those sorted in the specified manner, and put the
    // results into an initially empty ArrayList.
    ArrayList<Item> matchingItems = itemCollection
      .find(combinedFilter)
      .sort(sortingOrder)
      .into(new ArrayList<>());

    // Set the JSON body of the response to be the list of items returned by the database.
    // According to the Javalin documentation (https://javalin.io/documentation#context),
    // this calls result(jsonString), and also sets content type to json
    System.out.println(matchingItems.size());
    ctx.json(matchingItems);

    // Explicitly set the context status to OK
    ctx.status(HttpStatus.OK);
  }

  private Bson constructFilter(Context ctx) {
    List<Bson> filters = new ArrayList<>(); // start with a blank document
    // Combine the list of filters into a single filtering document.
    Bson combinedFilter = filters.isEmpty() ? new Document() : and(filters);
    return combinedFilter;
  }

  private Bson constructSortingOrder(Context ctx) {
    // Sort the results. Use the `sortby` query param (default "name")
    // as the field to sort by, and the query param `sortorder` (default
    // "asc") to specify the sort order.
    String sortBy = Objects.requireNonNullElse(ctx.queryParam("sortby"), "itemName");
    String sortOrder = Objects.requireNonNullElse(ctx.queryParam("sortorder"), "desc");
    Bson sortingOrder = sortOrder.equals("desc") ?  Sorts.descending(sortBy) : Sorts.ascending(sortBy);
    return sortingOrder;
  }

  public void addNewItem(Context ctx) {
    /*
     * The follow chain of statements uses the Javalin validator system
     * to verify that instance of `User` provided in this context is
     * a "legal" item. It checks the following things (in order):
     *    - itemType is valid
     *    - foodType is Valid
     */
    Item newItem = ctx.bodyValidator(Item.class)
      .check(req -> req.itemName.matches(ITEM_NAME_REGEX), "Item must contain valid item name").get();
    itemCollection.insertOne(newItem);

    ctx.json(Map.of("id", newItem._id));
    // 201 is the HTTP code for when we successfully
    // create a new resource (a item in this case).
    // See, e.g., https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
    // for a description of the various response codes.
    ctx.status(HttpStatus.CREATED);
  }
}

