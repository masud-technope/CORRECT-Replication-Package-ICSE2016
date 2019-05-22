# CORRECT: Code Reviewer Recommendation Based on Cross-Project & Technology Experience

Accepted Papers at ICSE 2016 (SEIP) and ASE 2016
-----------------------------------------------
```
CORRECT: Code Reviewer Recommendation in GitHub Based on Cross-Project and Technology Experience
Mohammad Masudur Rahman, Chanchal K. Roy, and Jason Collins
```
**Download this paper:**  [<img src="http://homepage.usask.ca/~masud.rahman/img/pdf.png"
     alt="PDF" heigh="16px" width="16px" />](http://homepage.usask.ca/~masud.rahman/papers/masud-ICSE2016.pdf)
```
CORRECT: Code Reviewer Recommendation at GitHub for Vendasta Technologies
Mohammad Masudur Rahman, Chanchal K. Roy, Jesse Redl, and Jason Collins
```
**Download this paper:**  [<img src="http://homepage.usask.ca/~masud.rahman/img/pdf.png"
     alt="PDF" heigh="16px" width="16px" />](http://homepage.usask.ca/~masud.rahman/papers/masud-ASE2016a-pp.pdf)

**Abstract:** Peer code review locates common coding rule violations and simple logical errors in the early phases of software development, and thus reduces overall cost. However, in GitHub, identifying an appropriate developer for code review during pull request submission is a non-trivial task. In this paper, we propose a heuristic ranking technique that considers not only the cross-project work history of a developer but also her experience in certain technologies associated with a pull request for determining her expertise as a potential code reviewer. We first motivate our technique using an exploratory study with 20 commercial projects. We then evaluate the technique using 13,081 pull requests from ten projects, and report 92.15% accuracy, 85.93% precision and 81.39% recall in code reviewer recommendation which outperforms the state-of-the-art technique.

Experimental Data
------------------------
- ```COMMIT:``` Commit SHA and corresponding changed files.
- ```JSON:``` Changed files and tokens from their import statements.
- ```PR:``` Pull request details
	- PR ID
	- Actual code reviewers
	- Suggested code reviewers
	- Commit SHAs

- ```sample-prlist:``` Input PR numbers
- ```sample-output:``` Suggested code reviewers for the input PR list

**The dataset is anonymized due to proprietary issues. More extended datasets are coming soon!**

Working Prototype
----------------------
- ```correct-exec:``` The working prototype for CORRECT


Please cite our work as
--------------------------
```
@inproceedings{icse2016masud, 
author = {Rahman, M. M. and Roy, C. K. and Collins, J.}, 
title = {{CORRECT: Code Reviewer Recommendation Based on Cross-Project and Technology Experience}}, 
booktitle = {Proc. ICSE-C}, 
year = {2016}, 
pages = {222--231} }
```
**Download this paper:**  [<img src="http://homepage.usask.ca/~masud.rahman/img/pdf.png"
     alt="PDF" heigh="16px" width="16px" />](http://homepage.usask.ca/~masud.rahman/papers/masud-ICSE2016.pdf)
```
@inproceedings{ase2016masud-correct, 
author = {Rahman, M. M. and Roy, C. K. and Redl, J and Collins, J.}, 
title = {{CORRECT: Code Reviewer Recommendation at GitHub for Vendasta Technologies}}, 
booktitle = {Proc. ASE}, 
year = {2016}, 
pages = {792--797} }
```
**Download this paper:**  [<img src="http://homepage.usask.ca/~masud.rahman/img/pdf.png"
     alt="PDF" heigh="16px" width="16px" />](http://homepage.usask.ca/~masud.rahman/papers/masud-ASE2016a-pp.pdf)

Do you also want to check [RevHelper](https://github.com/masud-technope/RevHelper-Replication-Package-MSR2017)?
----------------------

## Something not working as expected?

Contact:  **Masud Rahman**  ([masud.rahman@usask.ca](mailto:masud.rahman@usask.ca))

OR

Create an issue from  [here](https://github.com/masud-technope/CORRECT-Replication-Package-ICSE2016/issues/new)



