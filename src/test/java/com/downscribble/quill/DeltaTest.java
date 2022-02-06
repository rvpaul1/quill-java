package com.downscribble.quill;

import com.downscribble.quill.delta.AttributeMap;
import com.downscribble.quill.delta.Delta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;


/**
 * Test cases come from quill delta/test/delta version 4.2.2
 *
 * Check README for updates
 *
 * */
public class DeltaTest {

	//------------------Helper Method Tests-----------------------
	@Test
	public void testConcatEmptyDelta() throws Exception {
		Delta delta = new Delta().insert("Test");
		Delta concat = new Delta();
		Delta expected = new Delta().insert("Test");

		Assertions.assertEquals(delta.concat(concat), expected);
	}

	@Test
	public void testConcatUnmergeable() throws Exception {
		Delta delta = new Delta().insert("Test");
		Delta original = new Delta(delta);
		final AttributeMap attributes = new AttributeMap();
		attributes.put("bold", true);
		Delta concat = new Delta().insert("!", attributes);
		final AttributeMap expectedAttributes = new AttributeMap();
		expectedAttributes.put("bold", true);
		Delta expected = new Delta().insert("Test").insert("!", expectedAttributes);

		Assertions.assertEquals(delta.concat(concat), expected);
		Assertions.assertEquals(delta, original);
	}

	@Test
	public void testConcatMergeable() throws Exception {
		final AttributeMap attributes = new AttributeMap();
		attributes.put("bold", true);
		final AttributeMap expectedAttributes = new AttributeMap();
		expectedAttributes.put("bold", true);
		Delta delta = new Delta().insert("Test", attributes);
		Delta original = new Delta(delta);
		Delta concat = new Delta().insert("!", expectedAttributes).insert("\n");
		Delta expected = new Delta().insert("Test!", attributes).insert("\n");

		Assertions.assertEquals(delta.concat(concat), expected);
		Assertions.assertEquals(delta, original);
	}

	@Test
	public void testChopRetain() throws Exception {
		Delta delta = new Delta().insert("Test").retain(4);
		Delta expected = new Delta().insert("Test");

		Assertions.assertEquals(delta.chop(), expected);
	}

	@Test
	public void testChopInsert() throws Exception {
		Delta delta = new Delta().insert("Test");
		Delta expected = new Delta().insert("Test");

		Assertions.assertEquals(delta.chop(), expected);
	}

	@Test
	public void testChopFormattedRetain() throws Exception {
		AttributeMap attributeMap = new AttributeMap();
		attributeMap.put("bold", true);
		AttributeMap expectedAttributes = new AttributeMap();
		expectedAttributes.put("bold", true);

		Delta delta = new Delta().insert("Test").retain(4, attributeMap);
		Delta expected = new Delta().insert("Test").retain(4, expectedAttributes);

		Assertions.assertEquals(delta.chop(), expected);
	}

	/*
	* TODO Add eachLine tests here when method has been implemented.
	* */

	/*
	* TODO Add iteration method tests here when they have been implemented.
	* */

	/*
	 * TODO Add length method tests here when method has been implemented.
	 * */

	/*
	 * TODO Add changeLength method tests here when method has been implemented.
	 * */

	/*
	 * TODO Add slice method tests here when method has been implemented.
	 * */

	//----------------------------Compose method tests ----------------------

	@Test
	public void testComposeInsertInsert() throws Exception {
		Delta a = new Delta().insert("A");
		Delta b = new Delta().insert("B");
		Delta expected = new Delta().insert("B").insert("A");

		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeInsertRetain() {
		final AttributeMap attributes = new AttributeMap();
		attributes.put("bold", true);
		attributes.put("color", "red");
		attributes.put("font", null);

		final AttributeMap expectedAttributes = new AttributeMap();
		expectedAttributes.put("bold", true);
		expectedAttributes.put("color", "red");

		Delta a = new Delta().insert("A");
		Delta b = new Delta().retain(1, attributes);
		Delta expected = new Delta().insert("A", expectedAttributes);

		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeInsertDelete() {
		Delta a = new Delta().insert("A");
		Delta b = new Delta().delete(1);
		Delta expected = new Delta();

		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeDeleteInsert() {
		Delta a = new Delta().delete(1);
		Delta b = new Delta().insert("B");
		Delta expected = new Delta().insert("B").delete(1);

		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeDeleteRetain() {
		final AttributeMap attributes = new AttributeMap();
		attributes.put("bold", true);
		attributes.put("color", "red");

		final AttributeMap expectedAttributes = new AttributeMap();
		expectedAttributes.put("bold", true);
		expectedAttributes.put("color", "red");

		Delta a = new Delta().delete(1);
		Delta b = new Delta().retain(1, attributes);
		Delta expected = new Delta().delete(1).retain(1, expectedAttributes);

		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeDeleteDelete() {
		Delta a = new Delta().delete(1);
		Delta b = new Delta().delete(1);
		Delta expected = new Delta().delete(2);

		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeRetainInsert() {
		AttributeMap attributeMap = new AttributeMap();
		attributeMap.put("color", "blue");

		Delta a = new Delta().retain(1, attributeMap);
		Delta b = new Delta().insert("B");
		Delta expected = new Delta().insert("B").retain(1, attributeMap);

		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeRetainRetain() {
		AttributeMap attributeMap = new AttributeMap();
		attributeMap.put("color", "blue");

		AttributeMap attributeMap2 = new AttributeMap();
		attributeMap2.put("bold", true);
		attributeMap2.put("color", "red");
		attributeMap2.put("font", null);

		Delta a = new Delta().retain(1, attributeMap);
		Delta b = new Delta().retain(1, attributeMap2);
		Delta expected = new Delta().retain(1, attributeMap2);

		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testRetainDelete() {
		Delta a = new Delta().retain(1, buildMap("color", "blue"));
		Delta b = new Delta().delete(1);
		Delta expected = new Delta().delete(1);
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeInsertInMiddleOfText() {
		Delta a = new Delta().insert("Hello");
		Delta b = new Delta().retain(3).insert("X");
		Delta expected = new Delta().insert("HelXlo");
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeInsertAndDeleteOrdering() {
		Delta a = new Delta().insert("Hello");
		Delta b = new Delta().insert("Hello");
		Delta insertFirst = new Delta().retain(3).insert("X").delete(1);
		Delta deleteFirst = new Delta().retain(3).delete(1).insert("X");
		Delta expected = new Delta().insert("HelXo");

		Assertions.assertEquals(a.compose(insertFirst), expected);
		Assertions.assertEquals(b.compose(deleteFirst), expected);
	}

	@Test
	public void testComposeInsertEmbed() {
		Delta a = new Delta().insert(1, buildMap("src", "http://quilljs.com/image.png"));
		Delta b = new Delta().retain(1, buildMap("alt", "logo"));
		Delta expected = new Delta().insert(1, buildMap(
				"src", "http://quilljs.com/image.png",
				"alt", "logo"
		));
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeDeleteEntireText() {
		Delta a = new Delta().retain(4).insert("Hello");
		Delta b = new Delta().delete(9);
		Delta expected = new Delta().delete(4);
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeRetainMoreThanLengthOfText() {
		Delta a = new Delta().insert("Hello");
		Delta b = new Delta().retain(10);
		Delta expected = new Delta().insert("Hello");
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeRetainEmptyEmbed() {
		Delta a = new Delta().insert(1);
		Delta b = new Delta().retain(1);
		Delta expected = new Delta().insert(1);
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeRemoveAllAttributes() {
		Delta a = new Delta().insert("A", buildMap("bold", true));
		Delta b = new Delta().retain(1, buildMap("bold", null));
		Delta expected = new Delta().insert("A");
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeRemoveAllEmbedAttributes() {
		Delta a = new Delta().insert(2, buildMap("bold", true));
		Delta b = new Delta().retain(1, buildMap("bold", null));
		Delta expected = new Delta().insert(2);
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeImmutability() {
		AttributeMap attr1 = buildMap("bold", true);
		AttributeMap attr2 = buildMap("bold", true);
		Delta a1 = new Delta().insert("Test", attr1);
		Delta a2 = new Delta().insert("Test", attr1);
		Delta b1 = new Delta().retain(1, buildMap("color", "red")).delete(2);
		Delta b2 = new Delta().retain(1, buildMap("color", "red")).delete(2);
		Delta expected = new Delta()
				.insert("T", buildMap("color", "red", "bold", true))
				.insert("t", attr1);
		Assertions.assertEquals(a1.compose(b1), expected);
		Assertions.assertEquals(a1, a2);
		Assertions.assertEquals(b1, b2);
		Assertions.assertEquals(attr1, attr2);
	}

	@Test
	public void testComposeRetainStartOptimization() {
		Delta a = new Delta()
				.insert("A", buildMap("bold", true))
				.insert("B")
				.insert("C", buildMap("bold", true))
				.delete(1);
		Delta b = new Delta().retain(3).insert("D");
		Delta expected = new Delta()
				.insert("A", buildMap("bold", true))
				.insert("B")
				.insert("C", buildMap("bold", true))
				.insert("D")
				.delete(1);
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeRetainStartOptimizationSplit() {
		Delta a = new Delta()
				.insert("A", buildMap("bold", true))
				.insert("B")
				.insert("C", buildMap("bold", true))
				.retain(5)
				.delete(1);
		Delta b = new Delta().retain(4).insert("D");
		Delta expected = new Delta()
				.insert("A", buildMap("bold", true))
				.insert("B")
				.insert("C", buildMap("bold", true))
				.retain(1)
				.insert("D")
				.retain(4)
				.delete(1);
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeRetainEndOptimization() {
		Delta a = new Delta()
				.insert("A", buildMap("bold", true))
				.insert("B")
				.insert("C", buildMap("bold", true));
		Delta b = new Delta().delete(1);
		Delta expected = new Delta().insert("B").insert("C", buildMap("bold", true));
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeRetainEndOptimizationJoin() {
		Delta a = new Delta()
				.insert("A", buildMap("bold", true))
				.insert("B")
				.insert("C", buildMap("bold", true))
				.insert("D")
				.insert("E", buildMap("bold", true))
				.insert("F");
		Delta b = new Delta().retain(1).delete(1);
		Delta expected = new Delta()
				.insert("AC", buildMap("bold", true))
				.insert("D")
				.insert("E", buildMap("bold", true))
				.insert("F");
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeNullOther() {
		Delta a = new Delta().insert("A");
		Delta b = null;
		Delta expected = new Delta().insert("A");
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeNullOtherOps() {
		Delta a = new Delta().insert("A");
		Delta b = new Delta();
		Delta expected = new Delta().insert("A");
		Assertions.assertEquals(a.compose(b), expected);
	}

	@Test
	public void testComposeEmptyOtherOps() {
		Delta a = new Delta().insert("A");
		Delta b = new Delta(new ArrayList<>());
		Delta expected = new Delta().insert("A");
		Assertions.assertEquals(a.compose(b), expected);
	}

	private AttributeMap buildMap(Object... args) {
		final AttributeMap returnMap = new AttributeMap();

		for (int i = 0; i < args.length; i += 2) {
			returnMap.put((String) args[i], args[i + 1]);
		}

		return returnMap;
	}
}
