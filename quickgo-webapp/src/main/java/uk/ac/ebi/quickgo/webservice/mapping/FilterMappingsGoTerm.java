package uk.ac.ebi.quickgo.webservice.mapping;

import uk.ac.ebi.quickgo.solr.query.model.annotation.enums.AnnotationField;
import uk.ac.ebi.quickgo.web.util.annotation.AnnotationWSUtil;
import uk.ac.ebi.quickgo.webservice.definitions.FilterParameter;
import uk.ac.ebi.quickgo.webservice.definitions.FilterRequestDefinition;
import uk.ac.ebi.quickgo.webservice.model.Filter;

import java.util.List;

/**
 * @Author Tony Wardell
 * Date: 25/06/2015
 * Time: 13:57
 * Created with IntelliJ IDEA.
 */
public class FilterMappingsGoTerm extends FilterMapping {

	public static final String GO_ID_REG_EXP = "(go:|GO:|gO:|Go:)";
	private final AnnotationWSUtil annotationWSUtil;
	private boolean slimmingRequired=false;

	public FilterMappingsGoTerm(AnnotationWSUtil annotationWSUtil) {
		super(AnnotationField.ANCESTORSIPO);			//default
		this.annotationWSUtil = annotationWSUtil;
	}

	@Override
	public void processRequestObject(uk.ac.ebi.quickgo.webservice.model.FilterRequest filterRequest) {

		for( Filter aFilter : filterRequest.getList()) {

			//If there are GoIds add them to the list
			if(FilterRequestDefinition.GoID.getLowerCase().equals(aFilter.getType().toLowerCase())){
				args.add(aFilter.getValue());
			}

			//We need to get the go ids represented by this go slim set
			if(FilterRequestDefinition.GoSlim.getLowerCase().equals(aFilter.getType().toLowerCase())){
				List<String> idsForSlimSet = annotationWSUtil.goTermsForSlimSet(aFilter.getValue());
				args.addAll(idsForSlimSet);
			}

			//Otherwise we modify the mapping to use for this list of ids
			if(FilterRequestDefinition.GoTermUse.getLowerCase().equals(aFilter.getType().toLowerCase())){


				//The default to use for the solr field is AncestorIPO, so use this if the solr field hasn't already
				//been set. This has been set in the constructor


				//Check if slimming required
				if(FilterParameter.Slim.toLowerCase().equals(aFilter.getValue().toLowerCase())){
					slimmingRequired = true;
				}


				//Exact
				if(FilterParameter.Exact.toLowerCase().equals(aFilter.getValue().toLowerCase())){
					solrField=AnnotationField.GOID;
				}

			}
		}
	}



	public boolean useTermsAsGoSlim() {
		return slimmingRequired;
	}
}