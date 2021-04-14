package cn.krossframework.state;

import com.google.common.base.Preconditions;

public interface StateGroupPool {

    class FetchStateGroup {
        private final boolean isNew;
        private final StateGroup stateGroup;

        public FetchStateGroup(boolean isNew,
                               StateGroup stateGroup) {
            Preconditions.checkNotNull(stateGroup);
            this.isNew = isNew;
            this.stateGroup = stateGroup;
        }

        public StateGroup getStateGroup() {
            return stateGroup;
        }

        public boolean isNew() {
            return isNew;
        }
    }

    StateGroupFactory getStateGroupFactory();

    void setRemoveDeposedStateGroupPeriod(long period);

    void afterPropertiesSet() throws Exception;

    void removeDeposedStateGroup();

    FetchStateGroup findOrCreate(Long id);

    StateGroup find(long id);
}
