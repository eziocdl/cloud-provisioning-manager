CREATE TABLE provisioning_requests (
                                       id UUID PRIMARY KEY,
                                       requester_username VARCHAR(255) NOT NULL,
                                       ram VARCHAR(50) NOT NULL,
                                       cpu VARCHAR(50) NOT NULL,
                                       status VARCHAR(50) NOT NULL,
                                       created_at TIMESTAMP NOT NULL,
                                       updated_at TIMESTAMP
);