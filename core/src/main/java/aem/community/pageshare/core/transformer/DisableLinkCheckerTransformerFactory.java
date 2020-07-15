package aem.community.pageshare.core.transformer;


import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;


@Component(
        metatype = false,
        label = "Disable Link Checker Transformer Factory",
        description = "Disable Link Checker Transformer Factory transforms asset links in the page")
@Properties({
        @Property(name = "pipeline.type", value = "disableLinkCheckerTransformer", propertyPrivate = true)})
@Service(value = {TransformerFactory.class})
public class DisableLinkCheckerTransformerFactory implements TransformerFactory {

    @Override
    public Transformer createTransformer() {
        return new DisableLinkCheckerTransformer();
    }

}