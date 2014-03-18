package org.apache.marmotta.platform.ldp.webservices.testsuite;

import org.openrdf.model.URI;

import java.util.List;

/**
 * LDP Test Case
 *
 * @author Sergio Fernández
 * @see <a href="http://www.w3.org/TR/ldp-test-cases/#test-case-description">LDP Tests Cases</a>
 */
public class TestCase {

    /**
     * Test Case Uniform Resource Identifier
     */
    private URI uri;

    /**
     * rdfs:label. The human-readable label of the test.
     */
    private String label;

    /**
     * dc:title. The name of the test.
     */
    private String title;

    /**
     * dc:description. The description of the test.
     */
    private String description;

    /**
     * dc:contributor. The person (foaf:Person) contributing the test.
     */
    private URI contributor;

    /**
     * td:reviewStatus. The status of the test; possible status are: td:unreviewed, td:approved or td:rejected.
     */
    private URI reviewStatus;

    //td:specificationReference. An excerpt (tn:Excerpt) of the specification that is relevant to the test.

    /**
     * td:input. An input (tn:TestInput) used in the test.
     */
    private URI input;

    /**
     * td:precondition. A precondition that must be satisfied before running the test.
     */
    private URI precondition;

    /**
     * tn:output. An output (tn:TestOutput) to be produced by the test.
     */
    private URI output;

    /**
     * tn:testProcess. The list of steps (tn:Step) to be performed during the test.
     */
    private List<URI> testProcess;

    /**
     * tn:testAssertion. An assertion (tn:TestAssertion) to be performed over the test output.
     */
    private URI testAssertion;

    public TestCase(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URI getContributor() {
        return contributor;
    }

    public void setContributor(URI contributor) {
        this.contributor = contributor;
    }

    public URI getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(URI reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public URI getInput() {
        return input;
    }

    public void setInput(URI input) {
        this.input = input;
    }

    public URI getPrecondition() {
        return precondition;
    }

    public void setPrecondition(URI precondition) {
        this.precondition = precondition;
    }

    public URI getOutput() {
        return output;
    }

    public void setOutput(URI output) {
        this.output = output;
    }

    public List<URI> getTestProcess() {
        return testProcess;
    }

    public void setTestProcess(List<URI> testProcess) {
        this.testProcess = testProcess;
    }

    public URI getTestAssertion() {
        return testAssertion;
    }

    public void setTestAssertion(URI testAssertion) {
        this.testAssertion = testAssertion;
    }

}
