package misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

import msdbsearch.CompactFastaSequence;
import msdbsearch.CompactSuffixArray;
import msscorer.NewRankScorer;
import msscorer.NewScorerFactory;
import msscorer.NewScorerFactory.SpecDataType;
import msutil.ActivationMethod;
import msutil.AminoAcid;
import msutil.AminoAcidSet;
import msutil.Enzyme;
import msutil.InstrumentType;
import msutil.Protocol;

public class MSGFPlusPaper {
	public static void main(String argv[]) throws Exception
	{
//		nominalMassTable();
//		checkPeptidesWithNominalMassErrors();
		countTotalNumberOfPartitions();
	}

	public static void countTotalNumberOfPartitions() throws Exception
	{
		int numPartitions = 0;
		for(ActivationMethod method : ActivationMethod.getAllRegisteredActivationMethods())
		{
			if(method == ActivationMethod.FUSION || method == ActivationMethod.ASWRITTEN)
				continue;
			for(InstrumentType inst : InstrumentType.getAllRegisteredInstrumentTypes())
			{
				for(Enzyme enzyme : Enzyme.getAllRegisteredEnzymes())
				{
					for(Protocol protocol : Protocol.getAllRegisteredProtocols())
					{
						SpecDataType condition = new SpecDataType(method, inst, enzyme, protocol);
						InputStream is = ClassLoader.getSystemResourceAsStream("resources/ionstat/"+condition+".param");
						if(is != null)
						{
							System.out.println(condition);
							NewRankScorer scorer = new NewRankScorer(new BufferedInputStream(is));
							numPartitions += scorer.getParitionSet().size();
						}
					}
				}
			}
		}
		System.out.println("NumPartitions: " + numPartitions);
	}
	
	public static void nominalMassTable() throws Exception
	{
		String delimiter = " & ";
		System.out.println("Residue" + delimiter + "NominalMass" + delimiter + 
				"RealMass" + delimiter + "RescaledMass" + delimiter + 
				"Error(RealMass)" + delimiter + "Error(RescaledMass)"
				);
		AminoAcidSet aaSet = AminoAcidSet.getStandardAminoAcidSetWithFixedCarbamidomethylatedCys();
//		aaSet = AminoAcidSet.getStandardAminoAcidSet();
		float sumReal = 0;
		float sumRes = 0;
		float sumPPM = 0;
		float absSumReal = 0;
		float absSumRes = 0;
		for(AminoAcid aa : aaSet)
		{
			float mass = aa.getMass();
			int nominalMass = aa.getNominalMass();
			float rescaledMass = mass*0.9995f;
			float error = mass-nominalMass;
			float rescaledError = rescaledMass-nominalMass;
			float errorPPM = (rescaledMass-nominalMass)/mass*1e6f;
//			System.out.println(aa.getResidue()+delimiter+mass+delimiter+nominalMass+delimiter+rescaledMass+delimiter+error+delimiter+errorPPM+"\\\\");
			System.out.format("%c%s%d%s%.3f%s%.3f%s%.3f%s%.3f\\\\\n",
					aa.getResidue(),delimiter,
					nominalMass,delimiter,
					mass,delimiter,
					rescaledMass,delimiter,
					error,delimiter,
					rescaledError,delimiter
					);
			sumReal += error;
			sumRes += rescaledError;
			absSumReal += Math.abs(error);
			absSumRes += Math.abs(rescaledError);
			
			sumPPM += errorPPM;
		}
		System.out.println("AverageRealError\t"+sumReal/20);
		System.out.println("AverageRescaledError\t"+sumRes/20);
		System.out.println("AbsAverageRealError\t"+absSumReal/20);
		System.out.println("AbsAverageRescaledError\t"+absSumRes/20);
		System.out.println("AverageErrorPPM\t"+sumPPM/20);
	}
	
	public static void checkPeptidesWithNominalMassErrors() throws Exception
	{
		String fileName = System.getProperty("user.home")+"/Research/Data/IPI/IPI_human_3.87.fasta";
		int maxPeptideLength = 100;
		CompactFastaSequence fastaSequence = new CompactFastaSequence(fileName);
		CompactSuffixArray sa = new CompactSuffixArray(fastaSequence, maxPeptideLength);
		sa.measureNominalMassError(AminoAcidSet.getStandardAminoAcidSetWithFixedCarbamidomethylatedCys());
	}
}
