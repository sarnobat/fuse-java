import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import net.fusejna.DirectoryFiller;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Multimap;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class FuseYurl extends FuseFilesystemAdapterFull
{
	public static void main(String... args) throws FuseException
	{
		// Strange - groovy ignores arg1's hardcoding. Maybe it's not an acceptable array initialization in groovy? 
		final String[] args1 = args;//{ "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
		if (args.length != 1) {
			System.err.println("Usage: HelloFS <mountpoint>");
			System.exit(1);
//			args = new String[]{ "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
		}
		new FuseYurl().log(true).mount(args[0]);
	}

	final String filename = "/hello1.txt";
	final String contents = "Hello World!\n";

	@Override
	public int getattr(final String path, final StatWrapper stat)
	{
		if (path.equals(File.separator)) { // Root directory
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		} else {
			stat.setMode(NodeType.FILE).size(contents.length());
			return 0;
		}
//		return -ErrorCodes.ENOENT();
	}

	@Override
	public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info)
	{
		// Compute substring that we are being asked to read
		final String s = contents.substring((int) offset,
				(int) Math.max(offset, Math.min(contents.length() - offset, offset + size)));
		buffer.put(s.getBytes());
		return s.getBytes().length;
	}

	@Override
	public int readdir(final String path, final DirectoryFiller filler)
	{
		filler.add(filename);
		filler.add("sridhar.txt");
		return 0;
	}
	
	private static class Yurl {
		
		private static final Integer ROOT_ID = 45;
		private static JSONObject categoriesTreeCache;

		private static JSONObject getItemsAtLevelAndChildLevels(Integer iRootId) throws JSONException, IOException {
//			System.out.println("getItemsAtLevelAndChildLevels() - " + iRootId);
			if (categoriesTreeCache == null) {
				categoriesTreeCache = CategoryTree.getCategoriesTree(ROOT_ID);
			}
			// TODO: the source is null clause should be obsoleted
			JSONObject theQueryResultJson = execute(
					"START source=node({rootId}) "
							+ "MATCH p = source-[r:CONTAINS*1..2]->u "
							+ "WHERE (source is null or ID(source) = {rootId}) and not(has(u.type)) AND id(u) > 0  "
							+ "RETURN distinct ID(u),u.title,u.url, extract(n in nodes(p) | id(n)) as path,u.downloaded_video,u.downloaded_image,u.created,u.ordinal, u.biggest_image, u.user_image "
// TODO : do not hardcode the limit to 500. Category 38044 doesn't display more than 50 books since there are so many child items.
							+ "ORDER BY u.ordinal DESC LIMIT 500", ImmutableMap
							.<String, Object> builder().put("rootId", iRootId)
							.build(), "getItemsAtLevelAndChildLevels()");
			JSONArray theDataJson = (JSONArray) theQueryResultJson.get("data");
			JSONArray theUncategorizedNodesJson = new JSONArray();
			for (int i = 0; i < theDataJson.length(); i++) {
				JSONObject anUncategorizedNodeJsonObject = new JSONObject();
				_1: {
					JSONArray anItem = theDataJson.getJSONArray(i);
					_11: {
						String anId = (String) anItem.get(0);
						anUncategorizedNodeJsonObject.put("id", anId);
					}
					_12: {
						String aTitle = (String) anItem.get(1);
						anUncategorizedNodeJsonObject.put("title", aTitle);
					}
					_13: {
						String aUrl = (String) anItem.get(2);
						anUncategorizedNodeJsonObject.put("url", aUrl);
					}
					_14: {
						try {
							JSONArray path = (JSONArray) anItem.get(3);
							if (path.length() == 3) {
								anUncategorizedNodeJsonObject.put("parentId",
										path.get(1));
							} else if (path.length() == 2) {
								anUncategorizedNodeJsonObject.put("parentId",
										iRootId);
							}
							else if (path.length() == 1) {
								// This should never happen
								anUncategorizedNodeJsonObject.put("parentId",
										path.get(0));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					_15: {

						Object val = anItem.get(4);
						if (isNotNull(val)) {
							String aValue = (String) val;
							anUncategorizedNodeJsonObject.put("downloaded_video", aValue);
						}
					}
					_16: {
						Object val = anItem.get(6);
						if ("null".equals(val)) {
							System.out.println("Is null value");
						} else if (val == null) {
							System.out.println("Is null string");
						} else if (isNotNull(val)) {
							Long aValue = (Long) val;
							anUncategorizedNodeJsonObject.put("created", aValue);
						}
					}
					_17: {
						Object val = anItem.get(8);
						if (isNotNull(val)) {
							String aValue = (String) val;
							if ("null".equals(aValue)) {
								System.out.println("getItemsAtLevelAndChildLevels() - does this ever occur?");
							}
							anUncategorizedNodeJsonObject.put("biggest_image", aValue);
						}
					}
					_18: {
						Object val = anItem.get(9);
						if (isNotNull(val)) {
							String aValue = (String) val;
							if ("null".equals(aValue)) {
								System.out.println("getItemsAtLevelAndChildLevels() - does this ever occur?");
							}
							anUncategorizedNodeJsonObject.put("user_image", aValue);
						}
					}
				}
				theUncategorizedNodesJson.put(anUncategorizedNodeJsonObject);
			}
			
			JSONObject ret = new JSONObject();
			transform : {
				for (int i = 0; i < theUncategorizedNodesJson.length(); i++) {
					JSONObject jsonObject = (JSONObject) theUncategorizedNodesJson
							.get(i);
					String parentId = (String) jsonObject.get("parentId");
					if (!ret.has(parentId)) {
						ret.put(parentId, new JSONArray());
					}
					JSONArray target = (JSONArray) ret.get(parentId);
					target.put(jsonObject);
				}
			}
			return ret;
		}
		
	}
	private static JSONObject execute(String iCypherQuery,
			Map<String, Object> iParams, String... iCommentPrefix) throws IOException, JSONException {
		String commentPrefix = iCommentPrefix.length > 0 ? iCommentPrefix[0] + " " : "";
//		System.out.println(commentPrefix + "begin");
		System.out.println(commentPrefix + " - \n\t" + iCypherQuery + "\n\tparams - " + iParams);

		ClientConfig clientConfig = new DefaultClientConfig();
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
				Boolean.TRUE);

		// POST {} to the node entry point URI
		ClientResponse theResponse = Client.create(clientConfig).resource(
				CYPHER_URI)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).entity("{ }")
				.post(ClientResponse.class, ImmutableMap
						.<String, Object> of("query", iCypherQuery, "params",
								Preconditions.checkNotNull(iParams)));
		if (theResponse.getStatus() != 200) {
			System.out.println(commentPrefix + "FAILED:\n\t" + iCypherQuery + "\n\tparams: "
					+ iParams);
			throw new RuntimeException(IOUtils.toString(theResponse.getEntityInputStream()));
		}
		String theNeo4jResponse ;
		_1: {
			// Do not inline this. We need to close the stream after copying
			theNeo4jResponse = IOUtils.toString(theResponse.getEntityInputStream());
			theResponse.getEntityInputStream().close();
			theResponse.close();
		}
		System.out.println(commentPrefix + "end");
		return new JSONObject(theNeo4jResponse);
	}
	private static class CategoryTree {
		static JSONObject getCategoriesTree(Integer rootId)
				throws JSONException, IOException {
			return new AddSizes(
					// This is the expensive query, not the other one
					getCategorySizes(execute(
							"START n=node(*) MATCH n-->u WHERE has(n.name) "
									+ "RETURN id(n),count(u);",
							ImmutableMap.<String, Object>of(),
							"getCategoriesTree() [The expensive query]").getJSONArray(
							"data")))
					.apply(
					// Path to JSON conversion done in Cypher
					createCategoryTreeFromCypherResultPaths(
							// TODO: I don't think we need each path do we? We just need each parent-child relationship.
							execute("START n=node({parentId}) "
									+ "MATCH path=n-[r:CONTAINS*]->c "
									+ "WHERE has(c.name) "
									+ "RETURN extract(p in nodes(path)| "
									+ "'{ " + "id : ' + id(p) + ', "
									+ "name : \"'+ p.name +'\" , "
									+ "key : \"' + coalesce(p.key, '') + '\"" + " }'" + ")",
									ImmutableMap.<String, Object> of(
											"parentId", rootId),
									"getCategoriesTree() - [getting all paths 3]"),
							rootId));
		}
		
		private static Map<Integer, Integer> getCategorySizes(JSONArray counts) {
			Map<Integer, Integer> sizesMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < counts.length(); i++) {
				JSONArray row = counts.getJSONArray(i);
				sizesMap.put(row.getInt(0), row.getInt(1));
			}
			return ImmutableMap.copyOf(sizesMap);
		}
		

		private static JSONObject addSizesToCypherJsonResultObjects(JSONObject categoriesTree,
				Map<Integer, Integer> categorySizes) {
			Integer id = categoriesTree.getInt("id");
			categoriesTree.put("size", categorySizes.get(id));
			if (categoriesTree.has("children")) {
				JSONArray children = categoriesTree.getJSONArray("children");
				for (int i = 0; i < children.length(); i++) {
					addSizesToCypherJsonResultObjects(children.getJSONObject(i), categorySizes);
				}
			}
			return categoriesTree;
		}
		
		private static class AddSizes implements
				Function<JSONObject, JSONObject> {
			private final Map<Integer, Integer> categorySizes;

			AddSizes(Map<Integer, Integer> categorySizes) {
				this.categorySizes = categorySizes;
			}

			@Override
			public JSONObject apply(JSONObject input) {
				return addSizesToCypherJsonResultObjects(
						input, categorySizes);
			}
		}
		
		private static JSONObject createCategoryTreeFromCypherResultPaths(
				JSONObject theQueryJsonResult, Integer rootId) {
			JSONArray cypherRawResults = theQueryJsonResult
					.getJSONArray("data");
			checkState(cypherRawResults.length() > 0);
			Multimap<Integer, Integer> parentToChildren = buildParentToChildMultimap2(cypherRawResults);
			Map<Integer, JSONObject> categoryNodesWithoutChildren = createId(cypherRawResults);
			JSONObject root = categoryNodesWithoutChildren.get(rootId);
			root.put(
					"children",
					toJsonArray(buildChildren(parentToChildren.get(rootId),
							categoryNodesWithoutChildren, parentToChildren)));
			return root;
		}

		/**
		 * @return - a pair for each category node
		 */
		private static Map<Integer, JSONObject> createId(JSONArray cypherRawResults) {
			ImmutableMap.Builder<Integer, JSONObject> idToJsonBuilder = ImmutableMap
					.<Integer, JSONObject> builder();
			Set<Integer> seen = new HashSet<Integer>();
			for (int i = 0; i < cypherRawResults.length(); i++) {
				JSONArray treePath = cypherRawResults.getJSONArray(i).getJSONArray(0);
				for (int j = 0; j < treePath.length(); j++) {
					if (treePath.get(j).getClass().equals(JSON_OBJECT_NULL)) {
						continue;
					}
					JSONObject pathHopNode = new JSONObject(treePath.getString(j));//treePath.getString(j));
					int categoryId = pathHopNode.getInt("id");
					if (!seen.contains(categoryId)){
						seen.add(categoryId);
						idToJsonBuilder.put(categoryId,
								pathHopNode);
					}
				}
			}
			return idToJsonBuilder.build();
		}
		
		private static JSONArray removeNulls(JSONArray iJsonArray) {
			for(int i = 0; i < iJsonArray.length(); i++) {
				if (JSON_OBJECT_NULL.equals(iJsonArray.get(i))) {
					iJsonArray.remove(i);
					--i;
				}
			}
			return iJsonArray;
		}

		/**
		 * @return Integer to set of Integers
		 */
		private static Multimap<Integer, Integer> buildParentToChildMultimap2(
				JSONArray cypherRawResults) {
			Multimap<Integer, Integer> oParentToChildren = HashMultimap.create();
			getParentChildrenMap: {
				for (int pathNum = 0; pathNum < cypherRawResults.length(); pathNum++) {
					JSONArray categoryPath = removeNulls(cypherRawResults.getJSONArray(pathNum).getJSONArray(0));
					for (int hopNum = 0; hopNum < categoryPath.length() - 1; hopNum++) {
						if (categoryPath.get(hopNum).getClass().equals(JSON_OBJECT_NULL)) {
							continue;
						}
						if (categoryPath.get(hopNum + 1).getClass().equals(JSON_OBJECT_NULL)) {
							continue;
						}
						if (!(categoryPath.get(hopNum + 1) instanceof String)) {
							continue;
						}
						int childId = new JSONObject(
								categoryPath.getString(hopNum + 1)).getInt("id");
						int parentId = checkNotNull(new JSONObject(
								categoryPath
										.getString(hopNum)).getInt("id"));
						Object childrenObj = oParentToChildren.get(parentId);
						if (childrenObj != null) {
							Set<?> children = (Set<?>) childrenObj;
							if (!children.contains(childId)) {
								oParentToChildren.put(parentId, childId);
							}
						} else {
							oParentToChildren.put(parentId, childId);
						}
					}
				}
			}
			return oParentToChildren;
		}

		private static JSONArray toJsonArray(Collection<JSONObject> children) {
			JSONArray arr = new JSONArray();
			for (JSONObject child : children) {
				arr.put(child);
			}
			return arr;
		}

		private static Set<JSONObject> buildChildren(
				Collection<Integer> childIds, Map<Integer, JSONObject> nodes,
				Multimap<Integer, Integer> parentToChildren) {
			Builder<JSONObject> set = ImmutableSet.builder();
			for (int childId : childIds) {
				JSONObject childJson = nodes.get(childId);
				Collection<Integer> grandchildIds = parentToChildren
						.get(childId);
				Collection<JSONObject> grandchildNodes = buildChildren(
						grandchildIds, nodes, parentToChildren);
				JSONArray grandchildrenArray = toJsonArray(grandchildNodes);
				childJson.put("children", grandchildrenArray);
				set.add(childJson);
			}
			return set.build();
		}			
	}
	
	public static final Object JSON_OBJECT_NULL = JSONObject.Null;
	private static final String CYPHER_URI = "http://netgear.rohidekar.com:7474/db/data/cypher";

	private static boolean isNotNull(Object val) {
		return val != null && !("null".equals(val)) && !(val.getClass().equals(JSON_OBJECT_NULL));
	}

}
