/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormBaseComponent;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement.Layout;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer.FormLayout;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 13.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MultipleSelectionRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		MultipleSelectionComponent stC = (MultipleSelectionComponent)source;
		MultipleSelectionElementImpl stF = stC.getFormItem();
		if(stF.getLayout() == Layout.vertical) {
			int columns = stF.getColumns();
			if(columns <= 1) {
				renderVertical(sb, stC);
			} else {
				renderMultiColumnsVertical(sb, stC, columns);
			}
		} else if (stF.getLayout() == Layout.dropdown) {
			renderDropDown(sb, stC);
		} else {
			renderHorizontal(sb, stC);
		}
	}
	
	@Override
	protected String renderOpenFormComponent(StringOutput sb, Component source, String layout, Item item) {
		MultipleSelectionElementImpl stF = (MultipleSelectionElementImpl)item.getFormItem();
		if(stF.singleCheckWithoutValue()
				|| FormLayout.LAYOUT_TABLE_CONDENSED.layout().equals(layout)
				|| "label".equals(layout)) {
			return super.renderOpenFormComponent(sb, source, layout, item);
		}
		return renderOpenFormComponent(sb, "fieldset", source, layout,
				item.getElementCssClass(), item.hasError(), item.hasWarning(), item.hasFeedback());
	}

	@Override
	protected void renderLabel(StringOutput sb, FormBaseComponent component, String layout, Translator translator, String[] args) {
		MultipleSelectionElementImpl stF = (MultipleSelectionElementImpl)component.getFormItem();
		if(stF.singleCheckWithoutValue()
				|| FormLayout.LAYOUT_TABLE_CONDENSED.layout().equals(layout)
				|| "label".equals(layout)) {
			super.renderLabel(sb, component, layout, translator, args);
		} else {
			renderLabel(sb, "legend", component, translator, args);
		}
	}

	private void renderDropDown(StringOutput sb, MultipleSelectionComponent stC) {
		MultipleSelectionElementImpl stF = stC.getFormItem();
		long listId = CodeHelper.getRAMUniqueID();
		long buttonTitleId = CodeHelper.getRAMUniqueID();
		String buttonGroupId = "o_" + Long.toString(CodeHelper.getRAMUniqueID());
		
		sb.append("<div ");
		appendIdIfRequired(sb, stC).append(">");
		sb.append("<div id='").append(buttonGroupId).append("'");
		sb.append(" class='button-group dropdown");
		if (stF.getFormRequestEval().isTrue()) {
			// The menu should be open, if the component is dirty after a listener event
			sb.append(" open");
		}
		sb.append("'>");
		sb.append("<button type='button' class='btn btn-default dropdown-toggle o_button_printed o_ms_button' data-toggle='dropdown'");
		if (stF.hasLabel()) {
			sb.append(" aria-labelledby=\"o_cl").append(stC.getDispatchID()).append("\"");
		}
		sb.append(">");
		sb.append("<span id='").append(buttonTitleId).append("'></span>&nbsp;<span class='caret o_ms_carret'></span>");
		sb.append("</button>");
		sb.append("<ul class='dropdown-menu o_ms_list' id='");
		sb.append(listId);
		sb.append("'>");
		for(CheckboxElement check:stC.getCheckComponents()) {
			if(!check.isVisible()) {
				continue;
			}
			
			String subStrName = "name='" + check.getGroupingName() + "'";
			String formDispatchId = check.getFormDispatchId();
			String aId = "o_" + Long.toString(CodeHelper.getRAMUniqueID());
			String key = check.getKey();
			String value = check.getValue();
			boolean selected = check.isSelected();
			String cssClass = check.getCssClass();

			if(stF.isEscapeHtml()){
				key = StringHelper.escapeHtml(key);
				value = StringHelper.escapeHtml(value);
			}
				
			sb.append("<li class='");
			if (StringHelper.containsNonWhitespace(cssClass)) {
				sb.append(cssClass);
			}
			if(!stC.isEnabled() || !check.isEnabled()) {
				sb.append(" disabled");
			}
			sb.append("'>");
			sb.append("<label class='").append("' for=\"").append(formDispatchId).append("\" id='").append(aId).append("'");
			if(stF.isWithTitleOnLabels()) {
				sb.append(" title='").append(value).append("'");
			}
			sb.append(">");
			
			sb.append("<input type='checkbox' id='").append(formDispatchId).append("' ");
			sb.append(subStrName);
			sb.append(" value='").append(key).append("'");
			sb.append(" data-value='").append(StringHelper.escapeForHtmlAttribute(check.getValue())).append("'");
			sb.append(" data-checked='").append(selected).append("'");

			// Determine inherited from class
			boolean isInherited = cssClass != null && cssClass.contains("inherited");

			if (selected || isInherited) {
				sb.append(" checked='checked' ");
			}
			if(!stC.isEnabled() || !check.isEnabled() || isInherited) {
				sb.append(" disabled='disabled' ");
			}
			sb.append("> ");
			
			String iconLeftCSS = check.getIconLeftCSS();
			if (StringHelper.containsNonWhitespace(iconLeftCSS)) {
				sb.append(" <i class='").append(iconLeftCSS).append("'> </i> ");
			}
			if (StringHelper.containsNonWhitespace(value)) {
				sb.append(" ").append(value);		
			}
			
			sb.append("</label></li>");
			if(stC.isEnabled()){
				FormJSHelper.appendFlexiFormDirtyForClick(sb, stF.getRootForm(), aId);
			}
			
			// Set button text on ready
			sb.append("<script>\n")
			  .append("\"use strict\";\n")
			  .append("jQuery('#").append(buttonTitleId).append("').ready(function() {")
			  .append(getJsSetButtonText(stF, buttonTitleId, listId));
			if (stF.isBadgeStyle()) {
				sb.append(appendBadgeUpdaterScript(
						Long.toString(buttonTitleId),
						Long.toString(listId),
						stF.getNonSelectedText()
				));
			}
			  sb.append("});")
			  .append("</script>");
			
			if(stC.isEnabled() && check.isEnabled()) {
				// (un-) check ckechbox when clicking on the menu entry
				sb.append("<script>");
				sb.append("jQuery('#").append(aId).append("').on('click', function(event) {");
				sb.append("   var $target = jQuery(event.currentTarget);");
				sb.append("   var $inp = $target.find('input');");
				sb.append("   if ($inp.prop('disabled')) { event.preventDefault(); event.stopPropagation(); return false; }");
				sb.append("   var $check = !$inp.data('checked');");
				sb.append("   setTimeout( function() {");
				sb.append("     $inp.prop('checked', $check);");
				sb.append("     $inp.data('checked', $check);");
				sb.append(getJsSetButtonText(stF, buttonTitleId, listId));
				if (stF.isBadgeStyle()) {
					sb.append(appendBadgeUpdaterScript(
							Long.toString(buttonTitleId),
							Long.toString(listId),
							stF.getNonSelectedText()
					));
				}
				sb.append(getRawJSFor(check));
				sb.append(getAjaxOnlyJs(stF, stC, key));
				sb.append("   }, 0);");
				sb.append("   return false;");
				sb.append("});");
				// remember last focus position, see FormJSHelper.getRawJSFor()
				sb.append("jQuery('#").append(check.getFormDispatchId()).append("').on('focus',function(event) {");
				sb.append("o_info.lastFormFocusEl='").append(check.getFormDispatchId()).append("';"); 
				sb.append("});");
				sb.append("</script>");
			}
		}
		sb.append("</ul>");
		sb.append("</div>");
		sb.append("</div>");

		if(stC.isEnabled() && stF.isDropdownHiddenEventEnabled()) {
			sb.append("<script>");
			sb.append("jQuery('#").append(buttonGroupId).append("').on('hidden.bs.dropdown', function() {");
			sb.append(FormJSHelper.getXHRFnCallFor(stF.getRootForm(), stC.getFormDispatchId(), 1, false, false, false,
				new NameValuePair("dropdown-hidden", "true"))).append(";");
			sb.append("});");
			sb.append("</script>");
		}
	}

	private StringBuilder appendBadgeUpdaterScript(String buttonTitleId, String listId, String fallbackText) {
		String escapedFallback = StringHelper.escapeJavaScript(fallbackText);
		StringBuilder sb = new StringBuilder();

		sb.append("  var titleId = '").append(buttonTitleId).append("';\n")
				.append("  var listId = '").append(listId).append("';\n")
				.append("  var fallbackText = '").append(escapedFallback).append("';\n\n")
				.append("  function updateBadges() {\n")
				.append("    var pills = jQuery('#' + listId + ' li label input')\n")
				.append("      .filter(':checked')\n")
				.append("      .map(function() {\n")
				.append("        var $chk = jQuery(this);\n")
				.append("        var val = $chk.data('value');\n")
				.append("        var li = $chk.closest('li');\n")
				.append("        var isInherited = li.hasClass('inherited');\n")
				.append("        var isRoot = li.hasClass('root');\n")
				.append("        var iconClass = '';\n")
				.append("        if (isInherited) {\n")
				.append("          iconClass = 'o_icon o_icon_inheritance_inherited';\n")
				.append("        } else if (isRoot) {\n")
				.append("          iconClass = 'o_icon o_icon_inheritance_root';\n")
				.append("        }\n")
				.append("        var iconHtml = iconClass ? '<i class=\"' + iconClass + '\"></i>&nbsp;' : '';\n")
				.append("        return '<span class=\"o_labeled_light\">' + iconHtml + val + '</span>';\n")
				.append("      }).get().join(' ');\n")
				.append("    if (!pills) pills = fallbackText;\n")
				.append("    jQuery('#' + titleId).html(pills);\n")
				.append("  }\n")
				.append("  jQuery(document).ready(updateBadges);\n")
				.append("  jQuery(document).on('change', '#' + listId + \" input[type=checkbox]:not(:disabled)\", updateBadges);\n");

		return sb;
	}

	private StringBuilder getJsSetButtonText(MultipleSelectionElementImpl stF, long buttonTitleId, long listId) {
		StringBuilder sb = new StringBuilder();
		sb.append("jQuery('#").append(buttonTitleId).append("').text(function() {");
		sb.append("  var $buttonText = jQuery.makeArray(");
		sb.append("    jQuery('#").append(listId).append(" li label input').filter(':checked').map(function() {");
		sb.append("      return jQuery(this).data('value');");
		sb.append("    })");
		sb.append("  ).join(', ');");
		sb.append("  if ($buttonText == '') {");
		sb.append("    $buttonText = '").append(stF.getNonSelectedText()).append("';");
		sb.append("  }");
		sb.append("  return $buttonText;");
		sb.append("});");
		return sb;
	}

	private String getRawJSFor(CheckboxElement check) {
		StringBuilder eventHandlers = FormJSHelper.getRawJSFor(check.getRootForm(), check.getSelectionElementFormDispatchId(), check.getAction(), false, null, check.getFormDispatchId());
		String onKeyword = "onchange=";
		int onPos = eventHandlers.indexOf(onKeyword);
		String substring = "";
		if (onPos != -1) {
			// Strip onclick to use it in jQuery script.
			substring = eventHandlers.substring(onPos + onKeyword.length() + 1, eventHandlers.length() - 1);
		 }
		// Remove focus part
		onKeyword = "onfocus=";
		onPos = substring.indexOf(onKeyword);
		if (onPos != -1) {
			substring = substring.substring(0, onPos - 2);
		 }
		
		return substring;
	}
	
	private StringBuilder getAjaxOnlyJs(MultipleSelectionElement stF, MultipleSelectionComponent stC, String key) {
	StringBuilder sb = new StringBuilder();
	if (stF.isAjaxOnly()) {
		sb.append("if ($check) {");
		sb.append(FormJSHelper.getXHRFnCallFor(stF.getRootForm(), stC.getFormDispatchId(), 1, false, false, false,
	    		  new NameValuePair("achkbox", key), new NameValuePair("checked", "true"))).append(";");
		sb.append("} else {");
	    sb.append(FormJSHelper.getXHRFnCallFor(stF.getRootForm(), stC.getFormDispatchId(), 1, false, false, false,
	    		  new NameValuePair("achkbox", key), new NameValuePair("checked", "false"))).append(";");
		sb.append("}");
	}
	return sb;
	}

	private StringOutput appendIdIfRequired(StringOutput sb, MultipleSelectionComponent stC) {
		if(!stC.isDomReplacementWrapperRequired() && !stC.isDomLayoutWrapper()) {
			sb.append(" id='").append(stC.getDispatchID()).append("'");
		}
		return sb;
	}
	
	private void renderVertical(StringOutput sb, MultipleSelectionComponent stC) {
		for(CheckboxElement check:stC.getCheckComponents()) {
			if(check.isVisible()) {
				sb.append("<div ");
				appendIdIfRequired(sb, stC).append(" class='checkbox'>");
				renderCheckbox(sb, check, stC, false);
				sb.append("</div>");
			}
		}
	}

	private void renderMultiColumnsVertical(StringOutput sb, MultipleSelectionComponent stC, int columns) {
		String columnCss;
		if(columns == 2) {
			columnCss = "col-sm-6";
		} else if(columns == 3) {
			columnCss = "col-sm-4";
		} else {
			columns = 4;
			columnCss = "col-sm-3";
		}

		sb.append("<div ");
		appendIdIfRequired(sb, stC).append(">");
		CheckboxElement[] checks = stC.getCheckComponents();
		for(int i=0; i<checks.length; ) {
			sb.append("<div class='row checkbox'>");
			
			for(int j=columns; j-->0; ) {
				if(i < checks.length) {
					CheckboxElement check = checks[i++];
					sb.append("<div class='").append(columnCss).append("'>");
					renderCheckbox(sb, check, stC, false);
					sb.append("</div>");
				}
			}

			sb.append("</div>");
		}
		sb.append("</div>");
	}
	
	private void renderHorizontal(StringOutput sb, MultipleSelectionComponent stC) {
		sb.append("<").append("span", "div", stC.getSpanAsDomReplaceable());
		appendIdIfRequired(sb, stC).append(" class='form-inline'>");
		for(CheckboxElement check:stC.getCheckComponents()) {
			if(check.isVisible()) {
				renderCheckbox(sb, check, stC, true);
			}
		}
		sb.append("</").append("span", "div", stC.getSpanAsDomReplaceable()).append(">");
	}
	
	private void renderCheckbox(StringOutput sb, CheckboxElement check, MultipleSelectionComponent stC, boolean inline) {
		MultipleSelectionElementImpl stF = stC.getFormItem();

		String subStrName = "name='" + check.getGroupingName() + "'";
			
		String key = check.getKey();
		String value = check.getValue();
		if(stF.isEscapeHtml()){
			key = StringHelper.escapeHtml(key);
			value = StringHelper.escapeHtml(value);
		}
			
		boolean selected = check.isSelected();
		String formDispatchId = check.getFormDispatchId();
			
		//read write view
		String cssClass = check.getCssClass(); //optional CSS class
		sb.append("<div>", !inline); // normal checkboxes need a wrapper (bootstrap) ...
		sb.append("<label class='").append("checkbox-inline ", inline); // ... and inline a class on the label (bootstrap)			
		sb.append(" o_checkbox_h_aligned ", stF.isHorizontallyAlignedCheckboxes());
		sb.append(cssClass, cssClass != null).append("'");
		if(StringHelper.containsNonWhitespace(value)) {
			if (stF.getForId() == null) { // set 'for' attribute only if there is no 'for' attribute on element's label
				sb.append(" for='").append(formDispatchId).append("'");
			}
			if(stF.isWithTitleOnLabels()) {
				sb.append(" title='").append(value).append("'");
			}
		}
		sb.append(">");
		
		
		sb.append("<input type='checkbox' id='").append(formDispatchId).append("' ")
		  .append(subStrName)
		  .append(" value='").append(key).append("'");
		if (selected) {
			sb.append(" checked='checked' ");
		}
		if(!stC.isEnabled() || !check.isEnabled()) {
			sb.append(" disabled='disabled' ");
		} else if(stF.isAjaxOnly()) {
			// The implementation is conservative as it send the state of the check box,
			// this is especially useful if an issue of double evaluation appears.
			sb.append(" onclick=\"javascript: this.checked ?")
		      .append(FormJSHelper.getXHRFnCallFor(stF.getRootForm(), stC.getFormDispatchId(), 1, false, false, false,
		    		  new NameValuePair("achkbox", key), new NameValuePair("checked", "true")))
		      .append(" : ")
		      .append(FormJSHelper.getXHRFnCallFor(stF.getRootForm(), stC.getFormDispatchId(), 1, false, false, false,
		    		  new NameValuePair("achkbox", key), new NameValuePair("checked", "false")))
			  .append(";\"");
		} else {
			//use the selection form dispatch id and not the one of the element!
			sb.append(FormJSHelper.getRawJSFor(check.getRootForm(), check.getSelectionElementFormDispatchId(), check.getAction(), false, null, check.getFormDispatchId()));
			//TODO editor force focus to move on
			sb.append(" onmousedown=\"o_info.lastFormFocusEl='").append(check.getFormDispatchId()).append("';\"");
		}
		sb.append(">");
		String iconLeftCSS = check.getIconLeftCSS();
		if (StringHelper.containsNonWhitespace(iconLeftCSS)) {
			sb.append(" <i class='").append(iconLeftCSS).append("'> </i> ");
		}
		if (StringHelper.containsNonWhitespace(value)) {
			sb.append(" ").append(value);		
		} else if(inline) {
			// at least something in label required for properly aligned rendering, nbsp is important for bootstrap
			sb.append("&nbsp;"); 
		}
		sb.append("</label>")
		  .append(" ", inline) // to let a little space before the next checkbox
		  .append("</div>", !inline); // normal radios need a wrapper (bootstrap)
			
		if(stC.isEnabled()){
			//add set dirty form only if enabled
			FormJSHelper.appendFlexiFormDirtyForCheckbox(sb, stF.getRootForm(), formDispatchId);
			
			if(stC.getFormItem().getRootForm().isInlineValidationOn() || stC.getFormItem().isInlineValidationOn()) {
				FormJSHelper.appendValidationListeners(sb, stC.getFormItem().getRootForm(), formDispatchId, stC.getFormItem().getFormDispatchId());
			}
		}
	}
}