#include <iostream>
#include <fstream>
#include <string>
#include <stdio.h>
#include <vector>
#include <sstream>

#define N (2048*2048)
#define THREADS_PER_BLOCK 51

using namespace std;

__global__ void lastDigit(int *a, int *b) {
        int index = threadIdx.x + blockIdx.x * blockDim.x;
        b[index] = a[index]%10;
}

__global__ void find_maximum_kernel(int *array, int *min, int *mutex, unsigned int n)
{
	unsigned int index = threadIdx.x + blockIdx.x*blockDim.x;
	unsigned int stride = gridDim.x*blockDim.x;
	unsigned int offset = 0;

	__shared__ float cache[256];


	float temp = -1.0;
	while(index + offset < n){
	temp = fminf(temp, array[index + offset]);

	offset += stride;
	}

	cache[threadIdx.x] = temp;

	__syncthreads();


	// reduction
	unsigned int i = blockDim.x/2;
	while(i != 0){
		if(threadIdx.x < i){
			cache[threadIdx.x] = fminf(cache[threadIdx.x], cache[threadIdx.x + i]);
		}

	__syncthreads();
	i /= 2;
	}

	if(threadIdx.x == 0){
		while(atomicCAS(mutex,0,1) != 0);  //lock
			*min = fminf(*min, cache[0]);
	atomicExch(mutex, 0);  //unlock
	}
}



int main()
{
        std::ifstream ifs;
        ifs.open("inp.txt", std::ifstream::in);
        std::string temp;
        std::getline(ifs, temp);


        vector<int> vect;

        std::stringstream ss(temp);

        int i;

        while (ss >> i)
        {
                vect.push_back(i);

                if (ss.peek() == ',' || ss.peek() == ' ' )
                        ss.ignore();
        }

        ifs.close();
        int *h_array;
	int *d_array;
	int *h_min;
	int *d_min;
	int *d_mutex;
	
	h_array = (int*)malloc(N*sizeof(int));
	h_min = (int*)malloc(sizeof(int));
	cudaMalloc((void**)&d_array, N*sizeof(int));
	cudaMalloc((void**)&d_min, sizeof(int));
	cudaMalloc((void**)&d_mutex, sizeof(int));
	cudaMemset(d_min, 0, sizeof(int));
	cudaMemset(d_mutex, 0, sizeof(int));
	//populate array
	for(i = 0; i < vect.size(); i++){
               h_array[i] = vect[i]; 
        }

        int size = vect.size() * sizeof(int);
        

	cudaMemcpy(d_array, h_array, N*sizeof(int), cudaMemcpyHostToDevice);
	dim3 gridSize = 256;
	dim3 blockSize = 256;
	find_maximum_kernel<<< gridSize, blockSize >>>(d_array, d_min, d_mutex, N);
	
	cudaMemcpy(h_min, d_min, sizeof(float), cudaMemcpyDeviceToHost);

	ofstream q1a;
	q1a.open ("q1a.txt");
	q1a << *h_min;
	q1a.close();
	






	//part b
	int *d_b_array;
	int *h_array_b_solution;
	int *d_array_b_solution;
	
        //b = (int *)malloc(size);
	
	h_array_b_solution = (int*)malloc(N*sizeof(int));
        cudaMalloc((void **)&d_array_b_solution, size);
        cudaMalloc((void **)&d_b_array, size);
	
	cudaMemcpy(d_b_array, h_array, size, cudaMemcpyHostToDevice);
        lastDigit<<<N/THREADS_PER_BLOCK,THREADS_PER_BLOCK>>>(d_b_array, d_array_b_solution);

        cudaMemcpy(h_array_b_solution, d_array_b_solution, size, cudaMemcpyDeviceToHost);
        
	ofstream q1b;
	q1b.open ("q1b.txt");
	
	for(i = 0; i < vect.size(); i++){
                printf("%d, ", h_array_b_solution[i]);
        	q1b << h_array_b_solution[i] << ",";
	}
	q1b.close();

	free(h_array);
	free(h_min);
	free(h_array_b_solution);
	cudaFree(d_array);
	cudaFree(d_min);
	cudaFree(d_mutex);
	cudaFree(d_b_array);
	cudaFree(d_array_b_solution);
        return 0;
}
