/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


public class RenameGroupOperation implements Constants, Operation {

	@Override
	public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
		if (targets == null || targets.isEmpty() || targets.size() != 1) {
			return null;
		}

		final GraphContainer graphContainer = operationContext.getGraphContainer();

		final Window window = operationContext.getMainWindow();

		final Window groupNamePrompt = new Window("Rename Group");
		groupNamePrompt.setModal(true);
		groupNamePrompt.setResizable(false);
		groupNamePrompt.setHeight("180px");
		groupNamePrompt.setWidth("300px");

		// Define the fields for the form
		final PropertysetItem item = new PropertysetItem();
		item.addItemProperty("Group Label", new ObjectProperty<String>("", String.class));

		// TODO Add validator for groupname value

		final Form promptForm = new Form() {

			private static final long serialVersionUID = 9202531175744361407L;

			@Override
			public void commit() {
				super.commit();
				String groupLabel = (String)getField("Group Label").getValue();

				//Object parentKey = targets.get(0);
				//Object parentId = graphContainer.getVertexItemIdForVertexKey(parentKey);
				VertexRef parentId = targets.get(0);
				Vertex parentVertex = parentId == null ? null : graphContainer.getVertex(parentId);
				Item parentItem = parentVertex == null ? null : parentVertex.getItem();
				
				if (parentItem != null) {

					Property property = parentItem.getItemProperty("label");
					if (property != null && !property.isReadOnly()) {
						property.setValue(groupLabel);

						// Save the topology
						graphContainer.getDataSource().save(null);

						graphContainer.redoLayout();
					}
				}
			}
		};
		// Buffer changes to the datasource
		promptForm.setWriteThrough(false);
		promptForm.setItemDataSource(item);

		Button ok = new Button("OK");
		ok.addListener(new ClickListener() {

			private static final long serialVersionUID = 7388841001913090428L;

			@Override
			public void buttonClick(ClickEvent event) {
				promptForm.commit();
				// Close the prompt window
				window.removeWindow(groupNamePrompt);
			}
		});
		promptForm.getFooter().addComponent(ok);

		Button cancel = new Button("Cancel");
		cancel.addListener(new ClickListener() {

			private static final long serialVersionUID = 8780989646038333243L;

			@Override
			public void buttonClick(ClickEvent event) {
				// Close the prompt window
				window.removeWindow(groupNamePrompt);
			}
		});
		promptForm.getFooter().addComponent(cancel);

		groupNamePrompt.addComponent(promptForm);

		window.addWindow(groupNamePrompt);

		return null;
	}

	@Override
	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
		return targets != null && 
			targets.size() == 1 && 
			targets.get(0) != null 
		;
	}

	@Override
	public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
		// Only allow the operation on single non-leaf vertices (groups)
		return targets != null && 
			targets.size() == 1 && 
			targets.get(0) != null && 
			!operationContext.getGraphContainer().getVertex(targets.get(0)).isLeaf()
		;
	}

	@Override
	public String getId() {
		return "RenameGroup";
	}
}