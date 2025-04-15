# Simple Significance

**Simple, guided hypothesis testing.**

Simple Significance is a lightweight statistics web application built with Spring Boot and Java. It is a free, web-based tool for exploratory hypothesis testing — designed for anyone curious about basic data analysis, but most of all, for me to relearn a bit about statistics after not having thouched the subject at all since my master thesis research project back in 2013. :)

The app helps you compare group differences using statistical significance tests – without requiring advanced prior knowledge of statistics.

> 🔍 *Note: This tool is not intended to replace professional software like SPSS or R. The results are not meant for professional use, and accuracy is not guaranteed.*

---

## Features

- Upload a CSV file with two or more groups of numeric data
- Automatically checks group sizes, variance, and normality
- Suggests appropriate tests (T-Test, ANOVA, Mann-Whitney, etc.)
- Supports independent and paired data
- Displays p-values, effect size, skewness, and a plain-language summary
- Session-based language switching (English and Norwegian)
- Clean, responsive frontend with Thymeleaf and CSS


![projectData](https://github.com/user-attachments/assets/2bd20fc3-90ac-4c93-b52b-7a4667695aeb)
![SignificanceTestReport](https://github.com/user-attachments/assets/d560db5a-d66d-47a7-a5e8-7e99888f9b8e)
![GuideSection](https://github.com/user-attachments/assets/37ad11ff-a8ad-4ba5-b470-f9d048a960e3)

---

## Requirements

- **Java 21**
- **Maven** (or use the included wrapper: `./mvnw` or `mvnw.cmd`)

---

## Build & Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/simplesignificance.git
   cd simplesignificance
   ```

2. **Build the project**
   ```bash
   ./mvnw clean package
   ```

     ```cmd
   mvnw clean package
   ```

3. **Start the application**
   ```bash
   ./mvnw spring-boot:run
   ```

   ```cmd
   mvnw spring-boot:run
   ```

4. **Open in browser**
   Visit [http://localhost:8080](http://localhost:8080)

---

## Example CSV Format

```
Effect of mindfulness on cortison levels
Control,Experiment
10,23
11,1
15,23
etc.
```

- First row (optional): project title
- Second row (optional): group headers 
- Following rows: numeric values

More than two groups are allowed. The app will determine which tests are valid based on your data.

---

## Author

Christian Bjørnsrud  
📧 [ckb78@icloud.com](mailto:ckb78@icloud.com)  
🔗 [github.com/Bjornsrud](https://github.com/Bjornsrud)

---

