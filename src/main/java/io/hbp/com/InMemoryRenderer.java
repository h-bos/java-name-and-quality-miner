package io.hbp.com;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.AbstractAccumulatingRenderer;

import java.util.Iterator;

public class InMemoryRenderer extends AbstractAccumulatingRenderer
{
    public Iterator<RuleViolation> ruleViolations;
    public Iterator<Report.ProcessingError> processingErrors;

    public InMemoryRenderer()
    {
        // Doesn't matter.
        super("", "");
    }

    @Override
    public void end()
    {
        this.ruleViolations = this.report.iterator();
        this.processingErrors = this.report.errors();
    }


    @Override
    public String defaultFileExtension()
    {
        // Doesn't matter.
        return null;
    }
}
