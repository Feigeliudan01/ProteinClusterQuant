package edu.scripps.yates.pcq.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Set;

import edu.scripps.yates.pcq.compare.ComparisonInput.ComparisonType;
import edu.scripps.yates.utilities.venndata.ContainsMultipleKeys;
import edu.scripps.yates.utilities.venndata.VennDataForLists;
import gnu.trove.set.hash.THashSet;

public class PCQCompare {
	private final String name1;
	private final File file1;
	private final String name2;
	private final File file2;
	private final double threshold;
	private final String name3;
	private final File file3;
	private final ComparisonType comparisonType;

	public PCQCompare(ComparisonInput comparisonInput) {
		name1 = comparisonInput.getName(0);
		file1 = comparisonInput.getFile(0);
		name2 = comparisonInput.getName(1);
		file2 = comparisonInput.getFile(1);
		name3 = comparisonInput.getName(2);
		file3 = comparisonInput.getFile(2);
		threshold = comparisonInput.getThreshold();
		comparisonType = comparisonInput.getComparisonType();
	}

	private Set<ContainsMultipleKeys> readPeptideNodesFromDataFile(File file, double fdrThreshol) throws IOException {
		return readFromDataFile(file, "_", 0, fdrThreshol);
	}

	private Set<ContainsMultipleKeys> readProteinNodesFromDataFile(File file, double fdrThreshol) throws IOException {
		return readFromDataFile(file, ",", 2, fdrThreshol);
	}

	private Set<ContainsMultipleKeys> readFromDataFile(File file, String separator, int colIndex, double fdrThreshol)
			throws IOException {
		if (file == null) {
			return null;
		}
		Set<ContainsMultipleKeys> set = new THashSet<ContainsMultipleKeys>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));

			String line = null;
			boolean firstLine = true;
			while ((line = in.readLine()) != null) {
				if (firstLine) {
					firstLine = false;
					continue;
				}
				line = line.trim();
				// System.out.println(line);
				String string = line.split("\t")[colIndex].trim();

				if (!"".equals(string)) {
					final String fdrString = line.split("\t")[12];
					if (!"".equals(fdrString)) {
						Double fdr = Double.valueOf(fdrString);
						Double ratio = Double.valueOf(line.split("\t")[9]);
						Double variance = Double.valueOf(line.split("\t")[14]);
						int numPSMs = Integer.valueOf(line.split("\t")[6]);
						if (fdr <= fdrThreshol) {
							set.add(new ContainsFDRAndRatioItem(string, separator, fdr, ratio, variance, numPSMs));
						}
					}
				}

			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return set;
	}

	private void comparingPeptideNodes2DataSets() throws IOException {
		Set<ContainsMultipleKeys> set1 = readPeptideNodesFromDataFile(file1, threshold);
		Set<ContainsMultipleKeys> set2 = readPeptideNodesFromDataFile(file2, threshold);
		Set<ContainsMultipleKeys> set3 = readPeptideNodesFromDataFile(file3, threshold);
		String title = getTitle(name1, name2, name3);
		VennDataForLists<ContainsMultipleKeys> venn = new VennDataForLists<ContainsMultipleKeys>(title, name1, set1,
				name2, set2, name3, set3);
		writeResults(venn);
	}

	private String getTitle(String name1, String name2, String name3) {
		StringBuilder sb = new StringBuilder();
		if (name1 != null) {
			sb.append(name1);
		}
		if (!"".equals(sb.toString()) && name2 != null) {
			sb.append(" vs ");
		}
		if (name2 != null) {
			sb.append(name2);
		}
		if (!"".equals(sb.toString()) && name3 != null) {
			sb.append(" vs ");
		}
		if (name3 != null) {
			sb.append(name3);
		}
		return sb.toString();
	}

	private void writeAnalysisHeader() {
		String comparingString = "Comparing '" + name1 + "' vs '" + name2 + "'";
		if (name3 != null) {
			comparingString += " vs '" + name3 + "'";
		}
		System.out.println(comparingString);
		System.out.println("\n\n\n\n###############");
		System.out.println("Comparison made on: " + new Date());
		System.out.println("Datasets compared: ");
		System.out.println(name1 + "\t" + file1.getAbsolutePath());
		System.out.println(name2 + "\t" + file2.getAbsolutePath());
		if (file3 != null) {
			System.out.println(name3 + "\t" + file3.getAbsolutePath());
		}
		System.out.println();
	}

	private void comparingProteinNodes2DataSets() throws IOException {
		Set<ContainsMultipleKeys> set1 = readProteinNodesFromDataFile(file1, threshold);
		Set<ContainsMultipleKeys> set2 = readProteinNodesFromDataFile(file2, threshold);
		Set<ContainsMultipleKeys> set3 = readProteinNodesFromDataFile(file3, threshold);
		String title = getTitle(name1, name2, name3);
		VennDataForLists<ContainsMultipleKeys> venn = new VennDataForLists<ContainsMultipleKeys>(title, name1, set1,
				name2, set2, name3, set3);
		writeResults(venn);
	}

	private void writeResults(VennDataForLists<ContainsMultipleKeys> venn) throws MalformedURLException {
		writeAnalysisHeader();
		System.out
				.println("Venn diagram URL (copy and paste the following URL in a browser to get the Venn Diagram): ");
		System.out.println(venn.getImageURL());
		System.out.println(venn.getIntersectionsText());
		int i = 1;
		System.out
				.println("The following lists correspond to the different overlappings between the lists, printing the "
						+ comparisonType + " name and the FDR value: ");
		System.out.println();
		System.out.println("\nIntersection (" + venn.getIntersection123().size() + "): ");
		for (ContainsMultipleKeys containsMultipleKeys : venn.getIntersection123()) {
			System.out.println(i++ + "\t" + containsMultipleKeys);
		}
		i = 1;
		System.out.println("\nUnique to: " + name1 + " (" + venn.getUniqueTo1().size() + ")");
		for (ContainsMultipleKeys containsMultipleKeys : venn.getUniqueTo1()) {
			System.out.println(i++ + "\t" + containsMultipleKeys);
		}
		i = 1;
		System.out.println("\nUnique to: " + name2 + " (" + venn.getUniqueTo2().size() + ")");
		for (ContainsMultipleKeys containsMultipleKeys : venn.getUniqueTo2()) {
			System.out.println(i++ + "\t" + containsMultipleKeys);
		}

		if (name3 != null) {
			i = 1;
			System.out.println("\nUnique to: " + name3 + " (" + venn.getUniqueTo3().size() + ")");
			for (ContainsMultipleKeys containsMultipleKeys : venn.getUniqueTo3()) {
				System.out.println(i++ + "\t" + containsMultipleKeys);
			}
		}

	}

	public void runComparison() throws IOException {
		switch (comparisonType) {
		case PEPTIDE_NODES:
			comparingPeptideNodes2DataSets();
			break;
		case PROTEIN_NODES:
			comparingProteinNodes2DataSets();
			break;
		default:
			break;
		}

	}
}
