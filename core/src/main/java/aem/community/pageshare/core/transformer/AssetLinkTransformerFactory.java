package aem.community.pageshare.core.transformer;


import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(
        metatype = false,
        label = "Pageshare Asset Transformer Factory",
        description = "Pageshare Asset Transformer Factory transforms asset links in the page")
@Properties({
        @Property(name = "pipeline.type", value = "assetLinkTransformer", propertyPrivate = true)})
@Service(value = {TransformerFactory.class})
public class AssetLinkTransformerFactory implements TransformerFactory {

    private static final Logger log = LoggerFactory.getLogger(AssetLinkTransformerFactory.class);

    @Override
    public Transformer createTransformer() {
        log.error("loading >>>>>>>>>>>>>>>>>>>>");
        return new AssetLinkTransformer();
    }

}