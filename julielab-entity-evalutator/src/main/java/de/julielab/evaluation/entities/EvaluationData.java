package de.julielab.evaluation.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class EvaluationData extends ArrayList<EvaluationDataEntry> {

	private static final Logger log = LoggerFactory.getLogger(EvaluationData.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 7048898609514218622L;

	private boolean isMentionData;

	public static Comparator<EvaluationDataEntry> docIdComparator = new Comparator<EvaluationDataEntry>() {

		@Override
		public int compare(EvaluationDataEntry o1, EvaluationDataEntry o2) {
			return o1.getDocId().compareTo(o2.getDocId());
		}

	};

	public static Comparator<EvaluationDataEntry> offsetComparator = new Comparator<EvaluationDataEntry>() {

		@Override
		public int compare(EvaluationDataEntry o1, EvaluationDataEntry o2) {
			return Integer.compare(o1.getBegin(), o2.getBegin());
		}

	};

	private Multimap<String, EvaluationDataEntry> entriesByDocument;

	public EvaluationData() {
	}

	@Override
	public boolean add(EvaluationDataEntry entry) {
		checkMentionMode(entry);
		return super.add(entry);
	}

	protected void checkMentionMode(EvaluationDataEntry entry) {
		if (isEmpty()) {
			isMentionData = entry.isMention();
			if (entry.isMention())
				log.debug("Got first line \"{}\", treating file as delivered with offsets.", entry);
			else
				log.debug("Got first line \"{}\", treating file as delivered without offsets.", entry);
		} else if (isMentionData && !entry.isMention())
			throw new NoOffsetsException(
					"This data set is comprised of entity mentions with offset information. However, the new entry \""
							+ entry + "\" does not have valid offset information.");
	}

	protected void checkMentionMode() {
		if (size() > 0)
			isMentionData = get(0).isMention();
		for (EvaluationDataEntry entry : this) {
			if (isMentionData && !entry.isMention())
				throw new NoOffsetsException(
						"This data set is comprised of entity mentions with offset information. However, the new entry \""
								+ entry + "\" does not have valid offset information.");
		}
	}

	/**
	 * Minimal format is
	 * <pre>
	 * docId &lt;tab&gt; entityId
	 * </pre>
	 * Full format is
	 * <pre>
	 * docId &lt;tab&gt; entityId &lt;tab&gt; begin &lt;tab&gt; end &lt;tab&gt; text &lt;tab&gt; systemId
	 * </pre>
	 * Any subset of columns between the two may be given as long as the order is always correct.
	 * @param dataRecord
	 * @return
	 */
	public boolean add(String[] dataRecord) {
		if (dataRecord.length < 2)
			throw new IllegalArgumentException("Given data record \"" + Arrays.toString(dataRecord)
					+ "\" has less than two columns. The expected format is at least two columns where the first column is a document ID and the second is an entity ID to allow for the document-level evaluation of entity mention findings.");
		if (dataRecord.length < 3) {
			EvaluationDataEntry evalDataEntry = new EvaluationDataEntry(dataRecord[0], dataRecord[1]);
			return add(evalDataEntry);
		} else if (dataRecord.length > 2) {
			String docId = dataRecord[0];
			String entityId = dataRecord[1];
			int begin;
			int end;
			String entityString = null;
			String recognitionSystem = null;
			String confidence = null;
			try {
				begin = Integer.parseInt(dataRecord[2]);
			} catch (NumberFormatException e) {
				EvaluationDataEntry evalDataEntry = new EvaluationDataEntry(dataRecord[0], dataRecord[1]);
				entityString = dataRecord[2];
				if (dataRecord.length > 3)
					recognitionSystem = dataRecord[3];
				evalDataEntry.setEntityString(entityString);
				evalDataEntry.setRecognitionSystem(recognitionSystem);
				return add(evalDataEntry);
			}
			try {
				end = Integer.parseInt(dataRecord[3]);
			} catch (NumberFormatException e) {
				EvaluationDataEntry evalDataEntry = new EvaluationDataEntry(dataRecord[0], dataRecord[1]);
				entityString = dataRecord[2];
				if (dataRecord.length > 3)
					recognitionSystem = dataRecord[3];
				evalDataEntry.setEntityString(entityString);
				evalDataEntry.setRecognitionSystem(recognitionSystem);
				return add(evalDataEntry);
			}
			EvaluationDataEntry evalDataEntry = new EvaluationDataEntry(docId, entityId, begin, end);
			if (dataRecord.length > 4)
				entityString = dataRecord[4];
			if (dataRecord.length > 5)
				recognitionSystem = dataRecord[5];
			if (dataRecord.length > 6)
				confidence = dataRecord[6];
			evalDataEntry.setEntityString(entityString);
			evalDataEntry.setRecognitionSystem(recognitionSystem);
			evalDataEntry.setConfidence(confidence);
			return add(evalDataEntry);
		}
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends EvaluationDataEntry> c) {
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends EvaluationDataEntry> c) {
		return super.addAll(index, c);
	}

	@Override
	public void add(int index, EvaluationDataEntry element) {
		checkMentionMode(element);
		super.add(index, element);
	}

	public EvaluationData(List<String[]> records) {
		boolean hasOffsets = false;
		if (records.size() > 0) {
			add(records.get(0));
			EvaluationDataEntry lastEntry = get(size() - 1);
			hasOffsets = lastEntry.isMention();
			if (hasOffsets)
				log.debug("Got first line \"{}\", treating file as delivered with offsets.", records.get(0));
			else
				log.debug("Got first line \"{}\", treating file as delivered without offsets.", records.get(0));
		}
		for (int i = 1; i < records.size(); i++) {
			String[] record = records.get(i);
			add(record);
			EvaluationDataEntry lastEntry = get(size() - 1);
			if (hasOffsets && !lastEntry.isMention())
				throw new IllegalStateException("Input format error on line " + i
						+ ": Offset information expected, but not both begin and end offsets where found. The input format is <docId> <entityId> [<beginOffset> <endOffset>]. Line was: "
						+ Arrays.toString(record));
			// If the first entry didnt have offsets, we just ignore it when
			// something looks like it would.
			// else if (!hasOffsets && lastEntry.isMention())
			// throw new IllegalStateException(
			// "Input format error on line "
			// + i
			// +
			// ": No offset information expected, but there were more than two
			// columns. The input format is <docId> <entityId> [<beginOffset>
			// <endOffset>]. Line was: "
			// + Arrays.toString(record));
		}

		// if (records.size() > 0) {
		// hasOffsets = records.get(0).length > 2;
		// log.debug("Got first line \"{}\", treating file as delivered with
		// offsets.", records.get(0));
		// } else {
		// log.debug("Got first line \"{}\", treating file as delivered without
		// offsets.", records.get(0));
		// }
		// for (int i = 0; i < records.size(); i++) {
		// String[] dataRecord = records.get(i);
		// if (dataRecord.length < 4) {
		// if (hasOffsets)
		// throw new IllegalStateException(
		// "Input format error on line "
		// + i
		// +
		// ": Offset information expected, but there were less than four
		// columns. The input format is <docId> <entityId> [<beginOffset>
		// <endOffset>]. Line was: "
		// + Arrays.toString(dataRecord));
		// EvaluationDataEntry evalDataEntry = new
		// EvaluationDataEntry(dataRecord[0], dataRecord[1]);
		// add(evalDataEntry);
		// } else if (dataRecord.length > 2) {
		// if (!hasOffsets)
		// throw new IllegalStateException(
		// "Input format error on line "
		// + i
		// +
		// ": No offset information expected, but there were more than two
		// columns. The input format is <docId> <entityId> [<beginOffset>
		// <endOffset>]. Line was: "
		// + Arrays.toString(dataRecord));
		// String docId = dataRecord[0];
		// String entityId = dataRecord[1];
		// int begin;
		// int end;
		// try {
		// begin = Integer.parseInt(dataRecord[2]);
		// } catch (NumberFormatException e) {
		// throw new IllegalStateException("Input format error on line " + i
		// + ": Begin offset is no integer expression. Line was: " +
		// Arrays.toString(dataRecord));
		// }
		// try {
		// end = Integer.parseInt(dataRecord[3]);
		// } catch (NumberFormatException e) {
		// throw new IllegalStateException("Input format error on line " + i
		// + ": End offset is no integer expression. Line was: " +
		// Arrays.toString(dataRecord));
		// }
		// EvaluationDataEntry evalDataEntry = new EvaluationDataEntry(docId,
		// entityId, begin, end);
		// add(evalDataEntry);
		// }
		// }
	}

	public EvaluationData(String[]... data) {
		for (int i = 0; i < data.length; i++)
			add(data[i]);
	}

	public Multimap<String, EvaluationDataEntry> organizeByDocument() {
		// Collections.sort(this, docIdComparator);
		entriesByDocument = ArrayListMultimap.create();
		for (EvaluationDataEntry entry : this)
			entriesByDocument.put(entry.getDocId(), entry);
		return entriesByDocument;
	}

	public boolean isMentionData() {
		return isMentionData;
	}

	public Multimap<String, EvaluationDataEntry> getEntriesByDocument() {
		return entriesByDocument;
	}

	/**
	 * Reads a tab separated file and returns its contents
	 * <tt>EvaluationData</tt>.
	 * 
	 * @param dataFile
	 * @return
	 */
	public static EvaluationData readDataFile(File dataFile) {
		EvaluationData data = new EvaluationData();
		int i = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				i++;
				String[] splitLine = line.split("\t");
				data.add(splitLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoOffsetsException e) {
			log.error("Error while reading file \"{}\" on line {}: ",
					new Object[] { dataFile.getAbsolutePath(), i, e });
		}
		return data;
	}
}