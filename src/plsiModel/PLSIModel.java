package plsiModel;

import java.util.HashMap;
import java.util.Random;

public class PLSIModel {
	private HashMap<Integer, HashMap<Integer, Double>> dataSet;
	private int nOfTopics, nOfWords, nOfDocs;
	private double[] pZ;
	private double[][] pDZ;
	private double[][] pWZ;

	public PLSIModel() {
		// TODO Auto-generated constructor stub
	}

	public PLSIModel(HashMap<Integer, HashMap<Integer, Double>> inputData, int nOfWords, int nOfTopics, int nOfDocs) {
		dataSet = inputData;
		this.nOfDocs = nOfDocs;
		this.nOfTopics = nOfTopics;
		this.nOfWords = nOfWords;
		pZ = new double[nOfTopics];
		pDZ = new double[nOfDocs][nOfTopics];
		pWZ = new double[nOfWords][nOfTopics];

	}

	public void init() {
		System.out.println("Initialize Parameters:");
		initializeParameters();
		printParameters();
		trainingModel();
	}
	
	public void trainingModel() {
		/*double[][][] pZDW = new double[][][];
		for(int i = 0; i<1000; i++) {
			//E-step: Expectation step
			for(int t = 1; t<nOfTopics; t++) {
				for()
			}
			//M-step: Maximization step
			for(int i = 1; i<nOfTopics; i++) {
				for()
			}
		}
		*/
			    
			  
	}
	public void initializeParameters() {
		// randomly assign cluster for each pair of doc-word
		Random ran = new Random();
		HashMap<Integer, HashMap<Integer, Integer>> clusters = new HashMap<Integer, HashMap<Integer, Integer>>();
		int nOfRow = 0;
		for(int i = 0; i<dataSet.size(); i++) {
			// iterate each word in current document
			for(int j = 0; j<dataSet.get(i).size(); j++) {
				int cluster = ran.nextInt(nOfTopics);
				pZ[cluster]++;
				nOfRow++;
				pDZ[i][cluster]++;
				pWZ[j][cluster]++;
			}
			for(int j = 0; j<nOfTopics; j++) {
				pDZ[i][j] = pDZ[i][j]/dataSet.get(i).size();
			}
		}
		
		for(int i = 0; i<nOfTopics; i++) {
			pZ[i] = pZ[i]/nOfRow;
		}
		
		int sum;
		for(int i = 0; i<nOfWords; i++) {
			sum = 0;
			for(int j = 0; j<nOfTopics; j++) {
				sum +=pWZ[i][j];
			}
			for(int j = 0; j<nOfTopics; j++) {
				pWZ[i][j] = pWZ[i][j]/sum;
			}
		}
		
	}
	public void printParameters() {
		System.out.println("PZ:");
		for(int i = 0; i<nOfTopics; i++)
			System.out.println("Cluster: " +i+": "+pZ[i]);
		System.out.println("............................................");
		System.out.println("PDZ:");
		for(int i = 0; i<nOfDocs; i++) {
			for(int j = 0; j<nOfTopics; j++) {
				System.out.print(pDZ[i][j]+"\t");
			}
			System.out.println();
		}
		for(int i = 0; i<nOfWords; i++) {
			for(int j = 0; j<nOfTopics; j++) {
				System.out.print(pWZ[i][j]+"\t");
			}
			System.out.println();
		}
	}
}