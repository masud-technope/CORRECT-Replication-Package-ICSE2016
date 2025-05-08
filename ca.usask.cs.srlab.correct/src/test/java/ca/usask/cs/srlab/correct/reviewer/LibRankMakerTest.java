package ca.usask.cs.srlab.correct.reviewer;

import ca.usask.cs.srlab.correct.core.CorrectLocal;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;

public class LibRankMakerTest {

    String repoName;
    CorrectLocal correctLocal;

    @BeforeEach
    public void setup() {
        this.repoName = "sample";
        this.correctLocal = new CorrectLocal(this.repoName);
        this.correctLocal.cacheRepository();
    }

}
