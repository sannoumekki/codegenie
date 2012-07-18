package edu.uci.ics.mondego.codegenie.search.results;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;

import edu.uci.ics.mondego.codegenie.CodeGenieImages;
import edu.uci.ics.mondego.codegenie.search.SearchResultEntryWrapper;
import edu.uci.ics.mondego.codegenie.search.TestDrivenSearchResult;
import edu.uci.ics.mondego.codegenie.search.TestDrivenSearchQuery;

import edu.uci.ics.mondego.codegenie.search.results.ContentProvider;
import edu.uci.ics.mondego.codegenie.search.results.GroupAction;
import edu.uci.ics.mondego.codegenie.search.results.TDSearchResultLabelProvider;
import edu.uci.ics.mondego.codegenie.search.results.TDSearchResultPage;
import edu.uci.ics.mondego.codegenie.search.results.ShowNextResultsAction;
import edu.uci.ics.mondego.codegenie.search.results.SortAction;
import edu.uci.ics.mondego.codegenie.search.results.TableContentProvider;
//import edu.uci.ics.mondego.codegenie.search.results.TreeContentProvider;

public class TDSearchResultPage extends AbstractTextSearchViewPage implements
IAdaptable {
  public static final String KEY_GROUPING= "codegenie.search.searchresultviewpage.grouping"; 
  public static final String KEY_SORTING= "codegenie.search.searchresultviewpage.sorting";

  public static final String GROUP_FILTERING = "codegenie.search.searchresultviewpage.filtering"; //$NON-NLS-1$

  public static final int SORT_ORDER_RANK = 0; 
  public static final int SORT_ORDER_NAME = 1; 
  public static final int SORT_ORDER_CODEGENIE = 2;

  public static final int GROUP_BY_FILE = 4; 
  public static final int GROUP_BY_TYPE = 5; 

  //protected TreeContentProvider fTreeContentProvider;
  protected TableContentProvider fTableContentProvider;

  private GroupAction fGroupByFileAction;
  private GroupAction fGroupByTypeAction;

  protected int fCurrentGrouping;

  private SortAction fSortByCodeGenieAction;
  private int fCurrentSortOrder;


  private ShowNextResultsAction showNextResultsAction;
  private GetSliceAction getSliceAction;
  private IncludeSliceAction includeSliceAction;
  private WeaveSliceAction weaveSliceAction;
  private UnweaveSliceAction unweaveSliceAction;
  private TestSliceAction testSliceAction;
  private SnippetViewerSelectionListener snippetListener =
      new SnippetViewerSelectionListener();

  private UseReturnTypeAction useReturnTypeAction;
  private UseArgumentsAction useArgumentsAction;
  private UseNamesAction useNamesAction;
  private UseMissingClassNameAction useMissingClassNameAction;
  private UseSynonymsAction useSynonymsAction;
  private UseAntonymsAction useAntonymsAction;

  public TDSearchResultPage ()
  {
    super(FLAG_LAYOUT_FLAT);
    fCurrentGrouping = GROUP_BY_FILE;
    fCurrentSortOrder = SORT_ORDER_CODEGENIE;

  }

  public void createControl(Composite parent) {
    super.createControl(parent);
    this.getViewer().addSelectionChangedListener(snippetListener);
  }

  private void initGroupingActions() {
    fGroupByFileAction= new GroupAction("Group by file", "Group by file", this, 
        TDSearchResultPage.GROUP_BY_FILE); 
    fGroupByFileAction.setImageDescriptor(JavaUI.getSharedImages().
        getImageDescriptor(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CUNIT));

    fGroupByTypeAction= new GroupAction("Group by type", "Group by type", this, 
        TDSearchResultPage.GROUP_BY_TYPE); 
    fGroupByTypeAction.setImageDescriptor(JavaUI.getSharedImages().
        getImageDescriptor(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CLASS));
  }

  public int getGrouping () {
    return fCurrentGrouping;
  }

  public void setGrouping(int grouping) {
    fCurrentGrouping= grouping;
    updateGroupingActions();
    getSettings().put(KEY_GROUPING, fCurrentGrouping);
    getViewPart().updateLabel();
  }

  private void updateGroupingActions() {
    fGroupByFileAction.setChecked(fCurrentGrouping == TDSearchResultPage.GROUP_BY_FILE);
    fGroupByTypeAction.setChecked(fCurrentGrouping == TDSearchResultPage.GROUP_BY_TYPE);
  }

  private void initSortActions() {
    fSortByCodeGenieAction= new SortAction("Sort by CodeGenie rank", this, SORT_ORDER_CODEGENIE); //$NON-NLS-1$
    fSortByCodeGenieAction.setText("Sort by CodeGenie rank");
    fSortByCodeGenieAction.setToolTipText("Sort by rank");
    fSortByCodeGenieAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
        getImageDescriptor(ISharedImages.IMG_TOOL_UP));
  }


  private void initOtherActions ()
  {
    showNextResultsAction = new ShowNextResultsAction(this);
    showNextResultsAction.setText("Get next results");
    showNextResultsAction.setToolTipText("Get more results or run updated query");
    showNextResultsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
        getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));

    getSliceAction = new GetSliceAction(this.getSite());
    getSliceAction.setText("Get slice");
    getSliceAction.setEnabled(false);

    includeSliceAction = new IncludeSliceAction(this.getSite());
    includeSliceAction.setText("Include slice in a separate folder");

    weaveSliceAction = new WeaveSliceAction(this.getSite());
    weaveSliceAction.setText("Weave slice");

    unweaveSliceAction = new UnweaveSliceAction(this.getSite());
    unweaveSliceAction.setText("Unweave slice");

    testSliceAction = new TestSliceAction(this.getSite());
    testSliceAction.setText("Test slice");

    useReturnTypeAction = new UseReturnTypeAction();
    useReturnTypeAction.setText("R");
    useReturnTypeAction.setToolTipText("Use the return type in the query");
    useReturnTypeAction.setImageDescriptor(CodeGenieImages.create(CodeGenieImages.IMG_RETURN_TYPE));
    useReturnTypeAction.setChecked(true);

    useArgumentsAction = new UseArgumentsAction();
    useArgumentsAction.setText("A");
    useArgumentsAction.setToolTipText("Use the arguments in the query");
    useArgumentsAction.setImageDescriptor(CodeGenieImages.create(CodeGenieImages.IMG_ARGUMENTS));
    useArgumentsAction.setChecked(true);

    useNamesAction = new UseNamesAction();
    useNamesAction.setText("N");
    useNamesAction.setToolTipText("Use the name terms in the query");
    useNamesAction.setImageDescriptor(CodeGenieImages.create(CodeGenieImages.IMG_NAMES));
    useNamesAction.setChecked(true);
    
    useSynonymsAction = new UseSynonymsAction();
    useSynonymsAction.setText("Syn");
    useSynonymsAction.setToolTipText("Use synonym-based query expansion");
    //useSynonymsAction.setImageDescriptor(CodeGenieImages.create(CodeGenieImages.IMG_SYN));
    useSynonymsAction.setChecked(true);
    
    useAntonymsAction = new UseAntonymsAction();
    useAntonymsAction.setText("Ant");
    useAntonymsAction.setToolTipText("Use antonym-based query expansion");
    //useSynonymsAction.setImageDescriptor(CodeGenieImages.create(CodeGenieImages.IMG_SYN));
    useAntonymsAction.setChecked(true);


    useMissingClassNameAction = new UseMissingClassNameAction();
    useMissingClassNameAction.setText("Use class name terms as keywords");
    useMissingClassNameAction.setToolTipText("Use the class name terms as keywords for the query");
    useMissingClassNameAction.setChecked(true);
  }


  void setSortOrder(int order) {
    fCurrentSortOrder= order;
    StructuredViewer viewer= getViewer();
    viewer.getControl().setRedraw(false);

    if (order == SORT_ORDER_CODEGENIE) { 
      viewer.setSorter(new CodeGenieSorter());
    }
    viewer.getControl().setRedraw(true);
    getSettings().put(KEY_SORTING, fCurrentSortOrder);

    fSortByCodeGenieAction.setChecked(fCurrentSortOrder == fSortByCodeGenieAction.getSortOrder());
  }


  public void init(IPageSite site) {
    super.init(site);
    IMenuManager menuManager = site.getActionBars().getMenuManager();
    menuManager.insertBefore(IContextMenuConstants.GROUP_PROPERTIES, new Separator(GROUP_FILTERING));
    initGroupingActions();
    initSortActions ();
    initOtherActions ();
    contributeToActionBars();
  }


  //	private void addSortActions(IToolBarManager toolbarMgr) {
  //		toolbarMgr.add(fSortByCodeGenieAction);
  //	}
  //
  //	private void addGroupActions (IToolBarManager mgr)	{
  //		mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, new Separator(KEY_GROUPING));
  //		mgr.appendToGroup(KEY_GROUPING, fGroupByFileAction);
  //		mgr.appendToGroup(KEY_GROUPING, fGroupByTypeAction);
  //	}


  protected void fillContextMenu(IMenuManager mgr) {
    IStructuredSelection sel = (IStructuredSelection)this.getViewer().getSelection();

    if(sel.size() > 1) return;

    Object o = sel.getFirstElement();

    if (o instanceof SearchResultEntryWrapper) {
      SearchResultEntryWrapper sre = (SearchResultEntryWrapper)o;
      if(!sre.isWoven()) {
        if (!((TestDrivenSearchQuery)this.getInput().getQuery()).hasWoven()) {
          mgr.add(weaveSliceAction);
          mgr.add(includeSliceAction);
          //mgr.add(testSliceAction);
        }
      } else { 
        mgr.add(unweaveSliceAction);
        //if (sre.getTestResult() == null)
        mgr.add(testSliceAction);
      }
    }
  }

  protected void fillLocalPullDown (IMenuManager mgr) {
    mgr.add(useMissingClassNameAction);
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewPart().getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
  }


  protected void fillToolbar(IToolBarManager tbm) {
    super.fillToolbar(tbm);
    tbm.add(showNextResultsAction);
    tbm.add(useReturnTypeAction);
    tbm.add(useNamesAction);
    tbm.add(useArgumentsAction);
    tbm.add(useSynonymsAction);
    tbm.add(useAntonymsAction);
  }


  private void hookDoubleClickAction() {
    StructuredViewer viewer= getViewer();
    viewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        ISelection sel = event.getSelection();				
        if (sel instanceof IStructuredSelection)
        {
          IStructuredSelection structSel = ((IStructuredSelection) sel);
          weaveSliceAction.run(structSel.toArray());

        }
      }
    });
  }


  public void reportFileOpen () {

  }


  public void setViewPart(ISearchResultViewPart part) {
    super.setViewPart(part);
    //		fActionGroup= new NewSearchViewActionGroup(part);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  public Object getAdapter(Class adapter) {
    return null;
  }

  protected void configureTableViewer(TableViewer viewer) 
  {
    TableLayout layout = new TableLayout();
    Table table = viewer.getTable ();
    table.setLayout(layout);
    table.setHeaderVisible(false);
    viewer.setUseHashlookup(true);
    viewer.setLabelProvider(new TDSearchResultLabelProvider(this));
    fTableContentProvider=new TableContentProvider(this);
    viewer.setContentProvider(fTableContentProvider);
    setSortOrder(fCurrentSortOrder);
    hookDoubleClickAction ();
  }

  //	protected void configureTreeViewer(TreeViewer viewer) {
  //		viewer.setUseHashlookup(true);
  //		viewer.setLabelProvider(new TDSearchResultLabelProvider(this));
  //		fTreeContentProvider= new TreeContentProvider(this);
  //		viewer.setContentProvider(fTreeContentProvider);
  //		hookDoubleClickAction ();
  //	}

  public void clear() 
  {
    IContentProvider provider = getViewer().getContentProvider();

    if (provider instanceof ContentProvider) {
      ((ContentProvider)provider).clear ();
    }
  }

  protected void elementsChanged(Object[] objects) {
    IContentProvider provider = getViewer().getContentProvider();

    if (provider instanceof ContentProvider) {
      ((ContentProvider)provider).elementsChanged(objects);
    }
  }

  public void update(Object changedElement) {
    StructuredViewer viewer= getViewer();
    Object[] o = {changedElement};

    TableContentProvider tacp = (TableContentProvider) viewer.getContentProvider();
    tacp.elementsChanged(o);

    viewer.refresh();
  }

  public void refreshButtons() {
    useMissingClassNameAction.setChecked(true);
    useNamesAction.setChecked(true);
    useArgumentsAction.setChecked(true);
    useReturnTypeAction.setChecked(true);
    useSynonymsAction.setChecked(true);
    useAntonymsAction.setChecked(true);
  }

  public StructuredViewer getViewer() {
    return super.getViewer();
  }	

  public class UseReturnTypeAction extends Action {

    public void run () {
      // make sure that at least one type is selected
      if(!useNamesAction.isChecked() &&
          !useArgumentsAction.isChecked()) {
        this.setChecked(true);
        return;
      }
      TestDrivenSearchQuery tdsq = ((TestDrivenSearchResult)
          ((TDSearchResultPage)NewSearchUI.getSearchResultView().getActivePage())
          .getInput()).getSearchQuery();
      tdsq.setConsiderReturnType(!tdsq.isConsideringReturnType());
      useReturnTypeAction.setChecked(tdsq.isConsideringReturnType());

    }
  }



  public class UseArgumentsAction extends Action {

    public void run () {
      if(!useNamesAction.isChecked() &&
          !useReturnTypeAction.isChecked()) {
        this.setChecked(true);
        return;
      }
      TestDrivenSearchQuery tdsq = ((TestDrivenSearchResult)
          ((TDSearchResultPage)NewSearchUI.getSearchResultView().getActivePage())
          .getInput()).getSearchQuery();
      tdsq.setConsiderArguments(!tdsq.isConsideringArguments());
      useArgumentsAction.setChecked(tdsq.isConsideringArguments());
    }
  }

  public class UseNamesAction extends Action {

    public void run () {
      if(!useArgumentsAction.isChecked() &&
          !useReturnTypeAction.isChecked()) {
        this.setChecked(true);
        return;
      }
      TestDrivenSearchQuery tdsq = ((TestDrivenSearchResult)
          ((TDSearchResultPage)NewSearchUI.getSearchResultView().getActivePage())
          .getInput()).getSearchQuery();

      tdsq.setConsiderNames(!tdsq.isConsideringNames());
      useNamesAction.setChecked(tdsq.isConsideringNames());
    }
  }

  public class UseSynonymsAction extends Action {

    public void run () {
      TestDrivenSearchQuery tdsq = ((TestDrivenSearchResult)
          ((TDSearchResultPage)NewSearchUI.getSearchResultView().getActivePage())
          .getInput()).getSearchQuery();

      tdsq.setConsideringSynonyms(!tdsq.isConsideringSynonyms());
      useSynonymsAction.setChecked(tdsq.isConsideringSynonyms());
    }
  }
  
  public class UseAntonymsAction extends Action {

    public void run () {
      TestDrivenSearchQuery tdsq = ((TestDrivenSearchResult)
          ((TDSearchResultPage)NewSearchUI.getSearchResultView().getActivePage())
          .getInput()).getSearchQuery();

      tdsq.setConsideringAntonyms(!tdsq.isConsideringAntonyms());
      useAntonymsAction.setChecked(tdsq.isConsideringAntonyms());
    }
  }

  public class UseMissingClassNameAction extends Action {

    public void run () {
      TestDrivenSearchQuery tdsq = ((TestDrivenSearchResult)
          ((TDSearchResultPage)NewSearchUI.getSearchResultView().getActivePage())
          .getInput()).getSearchQuery();

      tdsq.setConsiderMissingClassName(!tdsq.isConsideringMissingClassName());
      useMissingClassNameAction.setChecked(tdsq.isConsideringMissingClassName());
    }
  }

  @Override
  protected void configureTreeViewer(TreeViewer arg0) {
    // TODO Auto-generated method stub

  }
}