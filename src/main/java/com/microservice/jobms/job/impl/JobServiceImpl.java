package com.microservice.jobms.job.impl;

import com.microservice.jobms.job.Job;
import com.microservice.jobms.job.JobRepository;
import com.microservice.jobms.job.JobService;
import com.microservice.jobms.job.clients.CompanyClient;
import com.microservice.jobms.job.clients.ReviewClient;
import com.microservice.jobms.job.dto.JobDTO;
import com.microservice.jobms.job.external.Company;
import com.microservice.jobms.job.external.Review;
import com.microservice.jobms.job.mapper.JobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    JobRepository jobRepository;

    @Autowired
    RestTemplate restTemplate;

    private CompanyClient companyClient;

    private ReviewClient reviewClient;

    public JobServiceImpl(JobRepository jobRepository, CompanyClient companyClient, ReviewClient reviewClient){
        this.jobRepository = jobRepository;
        this.companyClient =companyClient;
        this.reviewClient = reviewClient;
    }

    @Override
    public List<JobDTO> findAll() {
        List<Job> jobs = jobRepository.findAll();
        List<JobDTO> jobDTOS = new ArrayList<>();

        return jobs.stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private JobDTO convertToDTO(Job job){
       // RestTemplate restTemplate = new RestTemplate();

        Company company = companyClient.getCompany(job.getCompanyId());

        List<Review> reviews = reviewClient.getReviews(job.getCompanyId());

        JobDTO jobDTO = JobMapper.
                mapToJobWithCompanyDTO(job,company, reviews);


        //jobDTO.setCompany(company);
        return jobDTO;
    }


    @Override
    public void createJob(Job job) {
        jobRepository.save(job);
    }

    @Override
    public JobDTO getJobById(Long id) {
        Job job = jobRepository.findById(id).orElse(null);
        return convertToDTO(job);
    }

    @Override
    public boolean deleteJobById(Long id) {
        try {
            jobRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public boolean updateJob(Long id, Job updatedjob) {
        Optional<Job> jobOptional = jobRepository.findById(id);
        if(jobOptional.isPresent()){
            Job job = jobOptional.get();
            job.setTitle(updatedjob.getTitle());
            job.setDescription(updatedjob.getDescription());
            job.setMinSalary(updatedjob.getMinSalary());
            job.setMaxSalary(updatedjob.getMaxSalary());
            job.setLocation(updatedjob.getLocation());

            jobRepository.save(job);
            return true;
        }
        return false;
    }
}
