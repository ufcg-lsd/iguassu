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
import java.util.List;
import java.util.Objects;

import org.fogbowcloud.app.jdfcompiler.semantic.JDLCommand;

/**
 * Entity that encapsulates all the information given by the user about each task. To inform, the
 * user uses the Description Files that can be compiled by CommonCompiler.
 */
public class TaskSpecification implements Serializable {

    /**
     * Serial identification of the class. It need to be changed only if the class interface is
     * changed.
     */
    private static final long serialVersionUID = 1L;

    /** The sabotage check command. */
    private String sabotageCheck;

    /** Job source directory where results files are stored */
    private String sourceParentDir;

    private int taskSequenceNumber;

    private String expression;

    private List<JDLCommand> taskBlocks;

    /** The init part of a task specification. */
    private List<JDLCommand> initBlocks;

    /** The final part of a task specification. */
    private List<JDLCommand> finalBlocks;

    /**
     * Default empty constructor. FIXME Should not be needed in a near future.
     *
     * @param taskBlocks
     */
    public TaskSpecification(
            List<JDLCommand> initBlocks,
            List<JDLCommand> finalBlocks,
            List<JDLCommand> taskBlocks) {
        expression = null;
        this.initBlocks = initBlocks;
        this.finalBlocks = finalBlocks;
        this.taskBlocks = taskBlocks;
    }

    /**
     * Gets the init part of a task.
     *
     * @return Returns the initBlock.
     */
    public List<JDLCommand> getInitBlocks() {
        return initBlocks;
    }

    /**
     * Gets the final part of a task.
     *
     * @return Returns the finalBlock.
     */
    public List<JDLCommand> getFinalBlocks() {
        return finalBlocks;
    }

    /**
     * Gets the remote part of a task.
     *
     * @return Returns the remoteExec.
     */
    public List<JDLCommand> getTaskBlocks() {
        return taskBlocks;
    }

    /**
     * Gets the sabotage check command.
     *
     * @return
     */
    public String getSabotageCheck() {
        return this.sabotageCheck;
    }

    public void setSourceDirPath(String sourceParentDir) {

        this.sourceParentDir = sourceParentDir;
    }

    public String getSourceParentDir() {

        return this.sourceParentDir;
    }

    public void setTaskSequenceNumber(int taskSequenceNumber) {
        this.taskSequenceNumber = taskSequenceNumber;
    }

    public int getTaskSequenceNumber() {
        return taskSequenceNumber;
    }

    public void setSabotageCheck(String sabotageCheck) {
        this.sabotageCheck = sabotageCheck;
    }

    public void setInitBlocks(List<JDLCommand> initBlocks) {
        this.initBlocks = initBlocks;
    }

    public void setTaskBlocks(List<JDLCommand> taskBlocks) {
        this.taskBlocks = taskBlocks;
    }

    public void setFinalBlocks(List<JDLCommand> finalBlocks) {
        this.finalBlocks = finalBlocks;
    }

    public void setSourceParentDir(String sourceParentDir) {
        this.sourceParentDir = sourceParentDir;
    }

    /** @return the expression */
    public String getExpression() {
        return expression;
    }

    /** @param expression the expression to set */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskSpecification that = (TaskSpecification) o;

        if (taskSequenceNumber != that.taskSequenceNumber) return false;
        if (!Objects.equals(sabotageCheck, that.sabotageCheck)) return false;
        if (!Objects.equals(sourceParentDir, that.sourceParentDir)) return false;
        if (!Objects.equals(expression, that.expression)) return false;
        if (!Objects.equals(taskBlocks, that.taskBlocks)) return false;
        if (!Objects.equals(initBlocks, that.initBlocks)) return false;
        return Objects.equals(finalBlocks, that.finalBlocks);
    }

    @Override
    public int hashCode() {
        int result = sabotageCheck != null ? sabotageCheck.hashCode() : 0;
        result = 31 * result + (sourceParentDir != null ? sourceParentDir.hashCode() : 0);
        result = 31 * result + taskSequenceNumber;
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (taskBlocks != null ? taskBlocks.hashCode() : 0);
        result = 31 * result + (initBlocks != null ? initBlocks.hashCode() : 0);
        result = 31 * result + (finalBlocks != null ? finalBlocks.hashCode() : 0);
        return result;
    }
}
