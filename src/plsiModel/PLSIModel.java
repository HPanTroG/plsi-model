
import java.util.HashMap;
import java.util.Random;

public class PLSIModel {
	private HashMap<Integer, HashMap<Integer, Integer>> dataSet;
	private int nOfTopics, nOfWords, nOfDocs;
	private double[][] pDZ;
	private double[][] pWZ;
	private double[] pZ;
	private double[][][] pZDW;

	public PLSIModel() {
		// TODO Auto-generated constructor stub
	}

	public PLSIModel(HashMap<Integer, HashMap<Integer, Integer>> inputData, int nOfWords, int nOfTopics, int nOfDocs) {
		dataSet = inputData;
		this.nOfDocs = nOfDocs;
		this.nOfTopics = nOfTopics;
		this.nOfWords = nOfWords;
		pZ = new double[nOfTopics];
		pDZ = new double[nOfDocs][nOfTopics];
		pWZ = new double[nOfWords][nOfTopics];
		pZDW = new double[nOfTopics][nOfDocs][nOfWords];
		
	}
	public double[][][] getLearntTheta() {
		return pZDW;
	}
	public void trainingModel() {
		double sum;
		for (int i = 0; i < 20; i++) {

			// E-step: Expectation step
			System.out.println("+ E step:");
			for (int d = 0; d < nOfDocs; d++) {
				for (int w = 0; w < nOfWords; w++) {
					if (!dataSet.get(d).containsKey(w))
						continue;
					sum = 0;
					for (int z = 0; z < nOfTopics; z++) {
						
						pZDW[z][d][w] = pZ[z] * pDZ[d][z] * pWZ[w][z];
						sum += pZDW[z][d][w];
					}
					for (int z = 0; z < nOfTopics; z++) {
						pZDW[z][d][w] /= sum;
						//System.out.println(pZDW[z][d][w]);
					}
				}
			}

			// M-step: Maximization step
			System.out.println("+ M step:");
			// update p(w|z) = sum_d(n(d, w)*p(z|d, w))/sum_d(sum_w(n(d, w')*p(z|d, w')))
			for (int z = 0; z < nOfTopics; z++) {
				sum = 0;
				for (int w = 0; w < nOfWords; w++) {
					for (int d = 0; d < nOfDocs; d++) {
						if (!dataSet.get(d).containsKey(w))
							continue;
						pWZ[w][z] += dataSet.get(d).get(w) * pZDW[z][d][w];
					
					}
					sum += pWZ[w][z];
					
				
				}
				for (int w = 0; w < nOfWords; w++) {
					
					pWZ[w][z] /= sum;
					
				}

			}

			// update p(d|z) = sum(n(d, w')*p(z|d, w'))/sum(n(d', w')*p(z|d', w'))
			for (int z = 0; z < nOfTopics; z++) {
				sum = 0;
				for (int d = 0; d < nOfDocs; d++) {
					for (int w = 0; w < nOfWords; w++) {
						if (!dataSet.get(d).containsKey(w))
							continue;
						pDZ[d][z] += dataSet.get(d).get(w) * pZDW[z][d][w];
					}
					sum += pDZ[d][z];
				}
				for (int d = 0; d < nOfDocs; d++) {
					
					pDZ[d][z] /= sum;
				}
			}
			// update p(z) = sum(sum(n(d', w')P(z|d',w'))
			for(int z = 0; z<nOfTopics; z++) {
				sum = 0;
				for(int d = 0; d<nOfDocs; d++) {
					for(int w = 0; w<nOfWords; w++) {
						if(!dataSet.get(d).containsKey(w))
							continue;
						pZ[z] += dataSet.get(d).get(w) * pZDW[z][d][w];
						sum+= dataSet.get(d).get(w);
					}
				}
				pZ[z] /= sum;
			}

		}

	}

	public void init() {
		Random ran = new Random();
		double sum;

		// init p(z)
		for (int z = 0; z < nOfTopics; z++) {
			pZ[z] = (double)1 / nOfTopics;
		}

		// int pDZ
		for (int z = 0; z < nOfTopics; z++) {
			sum = 0;
			for (int d = 0; d < nOfDocs; d++) {
				pDZ[d][z] = ran.nextDouble();
				sum += pDZ[d][z];
			}
			for (int d = 0; d < nOfDocs; d++) {
				pDZ[d][z] /= sum;
			}

		}
		// init p(w|z),for each topic the constraint is sum(p(w|z))=1.0
		for (int z = 0; z < nOfTopics; z++) {
			sum = 0;
			for (int w = 0; w < nOfWords; w++) {
				pWZ[w][z] = ran.nextDouble();
				sum += pWZ[w][z];
			}
			for (int w = 0; w < nOfWords; w++) {
				pWZ[w][z] /= sum;
			}
		}

	}

	public void printParameters() {
		System.out.println("PZ:");
		for (int i = 0; i < nOfTopics; i++)
			System.out.println("Cluster: " + i + ": " + pZ[i]);
		System.out.println("............................................");
		System.out.println("PDZ:");
		for (int i = 0; i < nOfDocs; i++) {
			for (int j = 0; j < nOfTopics; j++) {
				System.out.print(pDZ[i][j] + "\t");
			}
			System.out.println();
		}
		for (int i = 0; i < nOfWords; i++) {
			for (int j = 0; j < nOfTopics; j++) {
				System.out.print(pWZ[i][j] + "\t");
			}
			System.out.println();
		}
	}
}