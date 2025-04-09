# SkillMatch

# Product Description

SkillMatch is a platform that connects service providers and customers, offering both web and mobile versions. Users can create profiles as either service providers or customers, with service providers showcasing their services and pricing ranges in a detailed portfolio. The platform includes real-time messaging, Google Maps integration for location tracking, a rating system for service providers, and a calendar for scheduling appointments. SkillMatch aims to simplify the process of finding, hiring, and managing service providers, providing a seamless and efficient user experience across devices.

# List of Features
Feature 1: Profile Creation  
Feautre 2: Portfolio Showcase   
Feature 3: Commenting System  
Feautre 4: Location Provider  
Feature 5: Rating System   
Feautre 6: Scheduling System  

# Links

Figma: https://www.figma.com/design/3PdvhYkWjYdEWIsHXuzfpf/SkillMatch?node-id=0-1&t=sitFKg1oQXeGV3l9-1     
Diagrams:    

# Developers Profile:

## Member 1

NAME: MARC ANDRE C. DOTAROT  
COURSE & YEAR: BSIT - 3

## DESCRIPTION

I am Marc Andre C. Dotarot and I am 22 years old. I am a student of Cebu Institute of Technology - University. I lived in Guadalupe Cebu City. I am currently taking BSIT and hope to work in the IT field one day. MY dream one day is to visit the United Kingdoms.

## MEMBER 2

NAME: FEERDEE ANNE C. LLABAN  
COURSE & YEAR: BSIT-3

# DESCRIPTION
I am 21 years of age.I am enthusiastic.I am from Tisa Cebu City. I am a third year BSIT Student. I go to gym.


# Features
- **Customer**: Book appointments with service providers.
- **Service Provider**: Create and manage portfolios to showcase services.

# Technologies
- **Frontend**: React (v19), Material UI, React Router, Axios
- **Backend**: Spring Boot, Spring Security, JPA/Hibernate, PostgreSQL

## Setup Instructions

# Prerequisites

Before setting up the project, ensure you have the following installed:
- **Node.js** (v14+)
- **Java** (v11+)
- **Maven**
- **PostgreSQL** (or any relational DB)

# Frontend Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/skillmatch-app.git
   cd skillmatch-app/frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure backend API URL in `.env`:
   ```bash
   REACT_APP_API_URL=http://localhost:5713
   ```

4. Run the frontend:
   ```bash
   npm run dev
   ```

# Backend Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/skillmatch-app.git
   cd skillmatch-app/backend
   ```

2. Configure database connection in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost: 3306/skillmatch_db
   spring.datasource.username=your_db_username
   spring.datasource.password=your_db_password
   ```

3. Build and run the backend:
   ```bash
   mvn spring-boot:run
   ```

# Dependencies

# Frontend Dependencies
- `@mui/material`, `react-router-dom`, `axios`
# Backend Dependencies
- `spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-devtools`, `spring-security-oauth2-jose`
# Usage Guide
1. **Customer**: Customers can browse and book appointments with service providers.

2. **Service Provider**: Service providers can create and manage their portfolios.

3. **API Endpoints**:
- `GET /api/portfolios/getPortfolio/{userId}`: Fetch portfolio by user.
- `POST /api/portfolios`: Create or update portfolio.
- `DELETE /api/portfolios/{portfolioId}`: Delete portfolio.
