/*
 * Copyright (C) 2008 Universidade Federal de Campina Grande
 *
 * This file is part of OurGrid.
 *
 * OurGrid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.fogbowcloud.app.core.models.job;

import java.io.Serializable;
import java.util.*;

import org.fogbowcloud.app.jdfcompiler.CommonUtils;
import org.fogbowcloud.app.jdfcompiler.exceptions.JobSpecificationException;

/**
 * Entity that encapsulates all the information given by the user about each job. To inform, the
 * user uses the Description Files that can be compiled by the CommonCompiler.
 *
 * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
 */
public class JobSpecification implements Serializable {

    /**
     * Serial identification of the class. It need to be changed only if the class interface is
     * changed.
     */
    private static final long serialVersionUID = 1L;

    /** A friendly name for the job. */
    private String label;

    /** */
    private String requirements;

    /** Describes the user preferences to be checked against the resources' annotations. */
    private Map<String, String> annotations;

    private List<TaskSpecification> taskSpecs;

    public JobSpecification() {}

    /**
     * Constructor.
     *
     * @param label The label for the job.
     * @param requirements The logical expression that defines the job. It will be used to choose
     *     machines that are able to run its tasks. To define it well, check the OurGrid manual.
     * @param taskSpecs A list with all the task specifications of this job.
     */
	private JobSpecification(
			String label,
			String requirements,
			List<TaskSpecification> taskSpecs,
			Map<String, String> annotations)
            throws JobSpecificationException {

        this.annotations = annotations;
        this.label = label;
        this.requirements = requirements;
        this.taskSpecs = taskSpecs;
        validate();
    }

    /** This method validates the attributes of this Job Spec */
    private void validate() throws JobSpecificationException {

        if (Objects.isNull(this.taskSpecs)) {
            throw new JobSpecificationException(
                    "A Job Spec could not be initialized with a null list of Task Specs.");
        }

        if (this.taskSpecs.size() == 0) {
            throw new JobSpecificationException(
                    "A Job Spec could not be initialized with an empty list of Task Specs.");
        }

        if (this.taskSpecs.contains(null)) {
            throw new JobSpecificationException(
                    "A Job Spec could not contain a null element into the list of Task Specs.");
        }
    }

    /**
     * The constructor
     *
     * @param label The label for the job.
     */
    public JobSpecification(String label) {
        this.label = label;
        this.requirements = "";
        this.taskSpecs = new ArrayList<>();
        this.annotations = CommonUtils.createSerializableMap();
    }

    /** @return A list with the tasks specification in this job. */
    public List<TaskSpecification> getTaskSpecs() {
        return taskSpecs;
    }

    /**
     * Inserts a list of task specifications.
     *
     * @param taskSpecs The list of tasks that will be contained by this job.
     * @throws JobSpecificationException
     */
    public void setTaskSpecs(List<TaskSpecification> taskSpecs) throws JobSpecificationException {
        this.taskSpecs = taskSpecs;
        validate();
    }

    /**
     * @return The logical expression that will be used to choose machines to run the tasks in this
     *     job.
     */
    public String getRequirements() {
        return this.requirements;
    }

    /**
     * Sets the logical expression for the job.
     *
     * @param expression The logical expression that defines the job. It will be used to choose
     *     machines that are able to run its tasks.
     */
    public void setRequirements(String expression) {
        this.requirements = expression;
    }

    /**
     * @return Gets the set of pair attribute=value defining the annotations for the job's
     *     preferences.
     */
    public Map<String, String> getAnnotations() {
        return this.annotations;
    }

    /**
     * Sets the set of pair attribute=value defining the annotations for the job's preferences.
     *
     * @param annotations Map of annotations
     */
    public void setAnnotations(Map<String, String> annotations) {

        this.annotations = annotations;
    }

    /** @return The label of the job. */
    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobSpecification that = (JobSpecification) o;

        if (!label.equals(that.label)) return false;
        if (!requirements.equals(that.requirements)) return false;
        if (!annotations.equals(that.annotations)) return false;
        return taskSpecs.equals(that.taskSpecs);
    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + requirements.hashCode();
        result = 31 * result + annotations.hashCode();
        result = 31 * result + taskSpecs.hashCode();
        return result;
    }
}
