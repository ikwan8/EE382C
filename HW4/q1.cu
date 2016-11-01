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
        int arr[vect.size()];
        std::copy(vect.begin(), vect.end(), arr);
        for(i = 0; i < vect.size(); i++){
                printf("%d, ", arr[i]);
        }

        int size = vect.size() * sizeof(int);
        int *d_b, *b;
        b = (int *)malloc(size);
        cudaMalloc((void **)&d_b, size);
        cudaMalloc((void **)&arr, size);
        cudaMemcpy(d_b, arr, size, cudaMemcpyHostToDevice);
        lastDigit<<<N/THREADS_PER_BLOCK,THREADS_PER_BLOCK>>>(arr, d_b);

        cudaMemcpy(b, d_b, size, cudaMemcpyDeviceToHost);
        
	ofstream q1b;
	q1b.open ("q1b.txt");
	
	for(i = 0; i < vect.size(); i++){
                printf("%d, ", b[i]);
        	q1b << b[i] << ",";
	}
	q1b.close();
        return 0;
}
