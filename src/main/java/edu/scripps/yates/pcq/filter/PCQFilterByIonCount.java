package edu.scripps.yates.pcq.filter;

import edu.scripps.yates.pcq.model.PCQPeptideNode;
import edu.scripps.yates.pcq.model.PCQProteinNode;
import edu.scripps.yates.pcq.util.PCQUtils;

public class PCQFilterByIonCount extends PCQFilter {
	private final int minIonCountPerNode;

	public PCQFilterByIonCount(int minIonCountPerNode) {
		this.minIonCountPerNode = minIonCountPerNode;
	}

	@Override
	protected boolean filter(PCQProteinNode proteinNode) {
		return true;

	}

	@Override
	protected boolean filter(PCQPeptideNode peptideNode) {
		final int ionCount = PCQUtils.getIonCount(peptideNode);
		if (ionCount >= minIonCountPerNode) {
			return true;
		}
		return false;
	}

	@Override
	protected boolean filterNonQuantifiedNodes() {
		return false;
	}

}
