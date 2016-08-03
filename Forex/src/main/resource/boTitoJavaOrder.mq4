//+------------------------------------------------------------------+
//|                                                   boTitoJava.mq4 |
//|                                              Andres Barale Sarti |
//|                                              http://www.mql5.com |
//+------------------------------------------------------------------+
#property copyright "Andres Barale Sarti"
#property link      "http://www.mql5.com"
#property version   "1.00"
#property strict

#property copyright "Andres Barale Sarti"
#property link      "mailto: andresbarale@gmail.com"

#define LR       "boTitoJavaOrder"


extern double profitPoint = 500.0;
extern double stopLossPoint = 300.00;
extern double LotsOpen = 0.01;
extern int magicNumber = 20160106;
extern string buyOpenString = "Buy";
extern string sellOpenString = "Sell";
extern int backTo = 500;
extern int windowing = 12;
extern int hiddenNeurons = 0;
extern bool atr = true;
extern bool cci = true;
extern bool macd = true;
extern bool rsi = true;
extern bool stoch = true;
extern bool wpr = true;
extern bool gator = true;
extern bool percentage = true; 
extern bool TENKANSEN = true;
extern bool KIJUNSEN  = true;
extern bool SENKOUSPANA = true;
extern bool SENKOUSPANB = true;
extern bool CHIKOUSPAN = true;

int firstTime = 0;
int buyer = 0;
int seller = 0;
bool buyOpen = false;
bool sellOpen = false;
bool stopOpen = false;
bool buyOpener = false;
bool sellOpener = false;
int stopFewSeconds = false;
int qp = 1;
int qp3 = 1;
int qp4 = 1;
int minuteWait = 47;
datetime timeCurrentOrder = TimeCurrent()-minuteWait*60;

//---- buffers
double BufferGreen[];
double BufferYellow[];
double BufferRed[];
double bidStart = 10000000;
double askStart;
int barsTotal = 0;
string   FileNameOrder = ".order";
string   FileName = ".csv";
string   FileNameLock = ".lock";
string   FileNamePropeties = ".properties";

//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+
void init()
{
   //closeOrders(1);
   seller = 0;
   buyer = 0;
   barsTotal = 0;
   //closeOrders(1);
}//int init() 
//+------------------------------------------------------------------+
void start()
{
   
   if ((Hour() >= 22 || Hour() <= 2)  ) { // 23 7 // 19 3 // || (Hour() >= 20 && DayOfWeek() == 5)
       closeOrders(1);
       seller = 0;
       buyer = 0;
       buyOpen = false;
       sellOpen = false;
       stopOpen = false;
       return; 
   }

   createCSV();
   readOrders();
   if (buyOpener) {      
      buy();
      buyOpener = false;
      sellOpener = false;
   } else if (sellOpener) {   
      sell();
      buyOpener = false;
      sellOpener = false;
   } else {
    
   }
    

   
}
//+------------------------------------------------------------------+

void buy(void) {

   if (!buyOpen) {  
      qp = 1;  
      qp3 = 1;
      closeOrders(1);
      openOrder(1);
      firstTime++;
   }
   buyer++ ;
   seller = 0;
   buyOpen = true;
   sellOpen = false;
}

void sell(void) {
  
   if (!sellOpen) { 
      qp = 1;  
      qp3 = 1;
      closeOrders(1);
      openOrder(0); 
      firstTime++;    
      timeCurrentOrder = TimeCurrent();
   }
   seller++;
   buyer = 0;
   buyOpen = false;
   sellOpen = true;
  
}
void readOrders(void) {
   datetime t = Time[0];
   string inditime =  
          StringConcatenate(TimeYear(t)+"-"+
                              TimeMonth(t)+"-"+
                             TimeDay(t)+"-"+
                              TimeHour(t)+"-"+
                              TimeMinute(t)+"0-"+
                              TimeSeconds(t)+0);
   //Print(Symbol()+ "-" + Period() + "-" + inditime + FileNameOrder);                    
   if(FileIsExist( Symbol()+ "-" + Period() + "-" + inditime + FileNameOrder,FILE_CSV|FILE_READ|FILE_WRITE)) {
         Print("Existe...");
         int    str_size;
         string str;
         ResetLastError();
         int file_handle=FileOpen(Symbol()+ "-" + Period() + "-" + inditime + FileNameOrder,FILE_CSV|FILE_READ|FILE_WRITE);
         if(file_handle!=INVALID_HANDLE)
           {
            //--- additional variables
          
            //--- read data from the file
            while(!FileIsEnding(file_handle))
              {
               //--- find out how many symbols are used for writing the time
               str_size=FileReadInteger(file_handle,INT_VALUE);
               //--- read the string
               str=FileReadString(file_handle,str_size);
               //--- print the string
               PrintFormat(str);
              }
            //--- close the file
            FileClose(file_handle);
            FileDelete(Symbol()+ "-" + Period() + "-" + inditime + FileNameOrder,FILE_CSV|FILE_READ|FILE_WRITE);
        }   
        if(str == buyOpenString) {
            buyOpener = true;
            sellOpener = false;
            buyOpen = false;
            int handle=FileOpen( Symbol()+ "-" + Period() + inditime+ FileNameOrder + ".old",FILE_CSV|FILE_READ|FILE_WRITE,",");
            FileWrite(handle ,str);
            FileClose(handle);
        } else if(str == sellOpenString) {
            buyOpener = false;
            sellOpener = true; 
            sellOpen = false ;
            int handle=FileOpen( Symbol()+ "-" + Period() + inditime+ FileNameOrder + ".old",FILE_CSV|FILE_READ|FILE_WRITE,",");
            FileWrite(handle ,str);
            FileClose(handle); 
        }
   }
}

void createCSV(void) {

      double period = Period();
      if(period < 0 || period > 1500.0) {
         return;
      }
      if (Bars <= barsTotal) {
         return;
      }
      barsTotal = Bars; 
      datetime t1 = Time[0];
      string inditimename = "-" +  
          StringConcatenate(TimeYear(t1)+"-"+
                              TimeMonth(t1)+"-"+
                             TimeDay(t1)+"-"+
                              TimeHour(t1)+"-"+
                              TimeMinute(t1)+"0-"+
                              TimeSeconds(t1)+0);
   //Prin
      //closeOrders(1);
      if(FileIsExist( Symbol()+ "-" + Period() + inditimename + FileName,FILE_CSV|FILE_READ|FILE_WRITE)) {

          FileDelete( Symbol()+ "-" + Period() + inditimename +FileName,FILE_CSV|FILE_READ|FILE_WRITE); 

          FileDelete(Symbol()+ "-" + Period() + inditimename + FileNameLock,FILE_CSV|FILE_READ|FILE_WRITE);

      }     

      //Define variables
      int limit,i;
      int counted_bars = IndicatorCounted();
      //Sleep(20 * 60 * 1000);

      //Make sure on most recent bar
      if(counted_bars>0) counted_bars--;
      //Set limit
      limit = Bars - counted_bars - 1;

      int handle1=FileOpen( Symbol()+ "-" + Period() + inditimename + FileNameLock,FILE_CSV|FILE_READ|FILE_WRITE,",");
      FileClose(handle1); 
       //Name column headers
      string header  = "OpenTimestamp"+ 0;
      int handle=FileOpen( Symbol()+ "-" + Period() + inditimename + FileName,FILE_CSV|FILE_READ|FILE_WRITE,",");
      for (int j = 0; j <= windowing; j++) {
         if (j > i) {
            
            header +=  ",direction"+j +",Open" + j + ",High"+ j +",Low" + j +",Close" + j;
            if(atr) header += ",atr"+ j; 
            if(cci) header += ",cci" + j;
            if(macd) header += ",macd"+ j; 
            if(rsi) header += ",rsi" + j;
            if(stoch) header += ",stoch"+j;
            if(wpr) header += ",wpr"+ j; 
            if(gator) header += ",gator"+ j;
            if(percentage) header +=  ",percentage"+ j;
            if(TENKANSEN) header += ",TENKANSEN" + j;
            if(KIJUNSEN) header += ",KIJUNSEN"+j;
            if(SENKOUSPANA) header += ",SENKOUSPANA"+ j; 
            if(SENKOUSPANB) header += ",SENKOUSPANB"+ j;
            if(CHIKOUSPAN) header +=  ",CHIKOUSPAN"+ j;
            header += ",timeHour"+j; 
            header += ",TimeDayOfWeek" + j;
         } else {
            header +=  ",direction" + j +",timeHour" + j + ",TimeDayOfWeek" + j + ",Open" + j ;
         }
      }   
      FileWrite(handle, header);
      for (i = backTo; i>=0; i--) {  
         datetime t = Time[i];
         string inditime =  
         StringConcatenate(TimeYear(t)+"-"+
                              TimeMonth(t)+"-"+
                             TimeDay(t)+"-"+
                              TimeHour(t)+"-"+
                              TimeMinute(t)+"0-"+
                              TimeSeconds(t)+0);
         string write =  inditime + ",";    
         FileSeek(handle,0,SEEK_END);
         if (TimeDayOfWeek(Time[i]) == 0) continue;
         for (int j = i; j<= i + windowing; j++) {
            int barind = j;
            int direction = -1;
            
            if((Close[barind] - Open[barind] ) > 0){
               direction = 1;
            } else {
               direction = 0;
            }
           
         
            if (j>i) {
               double tenka = NormalizeDouble(iIchimoku(Symbol(),0,9,26,52,MODE_TENKANSEN,barind), 4);
               double kijun = NormalizeDouble(iIchimoku(Symbol(),0,9,26,52,MODE_KIJUNSEN,barind), 4);
               double SENKOUA= NormalizeDouble(iIchimoku(Symbol(),0,9,26,52,MODE_SENKOUSPANA,barind), 4);
               double SENKOUB = NormalizeDouble(iIchimoku(Symbol(),0,9,26,52,MODE_SENKOUSPANB,barind), 4);
               double chiko =  NormalizeDouble(iIchimoku(Symbol(),0,9,26,52,MODE_CHIKOUSPAN,barind), 4);
               write += direction + "," + Open[barind]+ "," + High[barind]+ "," + Low[barind]+ "," + Close[barind]+ ",";
                  if(atr) write += iATR(Symbol(),0,14,barind)+ ","; 
                  if(cci) write += iCCI(Symbol(),0,14,PRICE_CLOSE,barind)+ ",";
                  if(macd) write += iMACD(Symbol(),0,12,26,9,PRICE_CLOSE,0,barind)+ ","; 
                  if(rsi) write += iRSI(Symbol(),0,14,PRICE_CLOSE,barind)+ ",";
                  if(stoch) write += iStochastic(Symbol(),0,5,3,3,MODE_EMA,0,0,barind)+ "," ;
                  if(wpr) write += iWPR(Symbol(),0,14,barind)+ ","; 
                  if(gator) write += iGator(NULL,0,13,8,8,5,5,3,MODE_SMMA,PRICE_MEDIAN,MODE_UPPER,1) + ",";
                  if(percentage) write +=  ((Close[barind] - Open[barind])/( Close[barind]))*100 + ",";
                  if(TENKANSEN) write += tenka  + ",";
                  if(KIJUNSEN) write += kijun + ",";
                  if(SENKOUSPANA) write += SENKOUA + ",";
                  if(SENKOUSPANB) write += SENKOUB + ",";
                  if(CHIKOUSPAN) write +=  chiko + ",";
               write += TimeHour(Time[barind])+ "," + TimeDayOfWeek(Time[barind]) + ",";
            } else {
               write += direction + "," + 
               TimeHour(Time[barind])+ "," + 
               TimeDayOfWeek(Time[barind]) + "," + 
               Open[barind]  + "," 
                ;
            }         

         }
         FileWrite(handle ,write);
      }

      FileClose(handle);
      FileDelete(Symbol()+ "-" + Period() + inditimename + FileNameLock,FILE_CSV|FILE_READ|FILE_WRITE); 
      return;
 }


void closeOrders(int closeDirect)                                     // Special function 'start'
  {
   string Symb=Symbol();                        // Symbol
   double Dist=1000000.0;                       // Presetting
   int Real_Order=-1;                           // No market orders yet
   double Win_Price=WindowPriceOnDropped();     // The script is dropped here
   string Text = "";
   double Price_Cls = 0.0;
   int Ticket = 0;
   double Lot = 0.0;
   
//-------------------------------------------------------------------------------- 2 --
   for(int i=1; i<=OrdersTotal(); i++)          // Order searching cycle
     {
      if (OrderSelect(i-1,SELECT_BY_POS)==true) // If the next is available
        {                                       // Order analysis:
         //----------------------------------------------------------------------- 3 --
         if (OrderSymbol()!= Symb) continue;    // Symbol is not ours
         int Tip=OrderType();                   // Order type
         if (Tip>1) continue;                   // Pending order  
         //----------------------------------------------------------------------- 4 --
         double Price=OrderOpenPrice();         // Order price
         if (NormalizeDouble(MathAbs(Price-Win_Price),Digits)< //Selection
            NormalizeDouble(Dist,Digits))       // of the closest order       
           {
            Dist=MathAbs(Price-Win_Price);      // New value
            Real_Order=Tip;                     // Market order available
            Ticket=OrderTicket();           // Order ticket
            Lot=OrderLots();             // Amount of lots
            double takeProfit = OrderProfit();
            //Print ("Actual profit: ", takeProfit);
          
            
         if (Real_Order==-1)                       // If no market orders available
            {
               Print("For ",Symb," no market orders available");
               break;                                 // Exit closing cycle        
            }
         int orderMagicNumber=OrderMagicNumber();
     
         if(orderMagicNumber!=magicNumber) continue;
         RefreshRates();    
         //-------------------------------------------------------------------------- 7 --
         switch(Real_Order)                        // By order type
           {
            case 0: Price_Cls=Bid;          // Order Buy
               Text="Buy ";                 // Text for Buy
               break;                              // Из switch
            case 1: Price_Cls=Ask;                 // Order Sell
               Text="Sell ";                       // Text for Sell
           }
            Print("Attempt to close ",Text," ",Ticket,". Awaiting response..");
            bool Ans=OrderClose(Ticket,Lot,Price_Cls,2);// Order closing
            //-------------------------------------------------------------------------- 8 --
            if (Ans==true)                            // Got it! :)
              {   
               Print ("Closed order ",Text," ",Ticket);
               stopFewSeconds = true;
              }
            //-------------------------------------------------------------------------- 9 --
            int Error=GetLastError();                 // Failed :(
            switch(Error)                             // Overcomable errors
              {
               case 135:Print("The price has changed. Retrying..");
                  RefreshRates();                     // Update data
                  continue;                           // At the next iteration
               case 136:Print("No prices. Waiting for a new tick..");
                  while(RefreshRates()==false)        // To the new tick
                     Sleep(1);                        // Cycle sleep
                  continue;                           // At the next iteration
               case 146:Print("Trading subsystem is busy. Retrying..");
                  Sleep(500);                         // Simple solution
                  RefreshRates();                     // Update data
                  continue;                           // At the next iteration
              }
            switch(Error)                             // Critical errors
              {
               case 2 : Print("Common error.");
                  break;                              // Exit 'switch'
               case 5 : Print("Old version of the client terminal.");
                  break;                              // Exit 'switch'
               case 64: Print("Account is blocked.");
                  break;                              // Exit 'switch'
               case 133:Print("Trading is prohibited");
                  break;      
               case 138:
                  closeOrders(closeDirect);
               break;                 
                         // Exit 'switch'
               default: Print("Occurred error ",Error);//Other alternatives   
              }
           }
         //----------------------------------------------------------------------- 5 --
        }                                       //End of order analysis
      }      //End of order searching
                                        
//-------------------------------------------------------------------------------- 6 --

//------------------------------------------------------------------------------- 10 --
   return;                                      // Exit start()
  }
//------------------------------------------------------------------------------- 11 --



//-------------------------------------------------------------------------------
// openbuy.mq4 
// The code should be used for educational purpose only.
//-------------------------------------------------------------------------- 1 --
void openOrder(int buy )                                     // Special function start
  {
   int Dist_SL =10;                             // Preset SL (pt)
   int Dist_TP =3;                              // Preset TP (pt)
   double Prots=0.35;                           // Percentage of free margin
   string Symb=Symbol();                        // Symbol
   double Lot= LotsOpen;
   double money = 20.0;
//-------------------------------------------------------------------------- 2 --
   while(true)                                  // Cycle that opens an order
     {
      double Min_Dist=MarketInfo(Symb,MODE_STOPLEVEL);// Min. distance
      double Min_Lot=MarketInfo(Symb,MODE_MINLOT);// Min. volume
      double Step   =MarketInfo(Symb,MODE_LOTSTEP);//Step to change lots
      double Free   =AccountFreeMargin();       // Free Margin
      double One_Lot=MarketInfo(Symb,MODE_MARGINREQUIRED);//Cost per 1 lot
      //-------------------------------------------------------------------- 3 --
   
      if (Lot < Min_Lot)                        // If it is less than allowed
        {
         Print(" Not enough money for ", Min_Lot," lots");
         break;                                 // Exit cycle
        }
      //-------------------------------------------------------------------- 4 --
      if (Dist_SL < Min_Dist)                   // If it is less than allowed
        {
         Dist_SL=Min_Dist;                      // Set the allowed
         Print(" Increased the distance of SL = ",Dist_SL," pt");
        }
      double SL=Bid - Dist_SL*Point;            // Requested price of SL
      //-------------------------------------------------------------------- 5 --
      if (Dist_TP < Min_Dist)                   // If it is less than allowed
        {
         Dist_TP=Min_Dist;                      // Set the allowed
         Print(" Increased the distance of TP = ",Dist_TP," pt");
        }
      double TP=Bid + Dist_TP*Point;            // Requested price of TP
      //-------------------------------------------------------------------- 6 --
      double minstoplevel=MarketInfo(Symbol(),MODE_STOPLEVEL);
      
      TP = 0.0;
      Print("The request was sent to the server. Waiting for reply..");
      int ticket = 0;
      RefreshRates();
      if (buy != 1) {
         SL = NormalizeDouble((Ask + Point * stopLossPoint),5);
         TP = NormalizeDouble((Bid - Point *  profitPoint),5);     
         ticket = OrderSend(Symb, OP_SELL, Lot,Bid, 1, SL, TP, NULL, magicNumber, 0);
         bidStart = Bid;
      } else {
         SL = NormalizeDouble((Bid - Point * stopLossPoint), 5);
         TP = NormalizeDouble((Ask + Point * profitPoint), 5);        
         ticket = OrderSend(Symb, OP_BUY, Lot, Ask, 1, SL, TP, NULL, magicNumber, 0);
         askStart = Ask;
      }
      //-------------------------------------------------------------------- 7 --
      if (ticket>0)                             // Got it!:)
        {
         Print ("Opened order Buy ",ticket);
         break;                                 // Exit cycle
        }
      //-------------------------------------------------------------------- 8 --
      int Error=GetLastError();                 // Failed :(
      switch(Error)                             // Overcomable errors
        {
         case 135:Print("The price has changed. Retrying..");
            RefreshRates();                     // Update data
            continue;                           // At the next iteration
         case 136:Print("No prices. Waiting for a new tick..");
            while(RefreshRates()==false)        // Up to a new tick
               Sleep(1);                        // Cycle delay
            continue;                           // At the next iteration
         case 146:Print("Trading subsystem is busy. Retrying..");
            Sleep(500);                         // Simple solution
            RefreshRates();                     // Update data
            continue;                           // At the next iteration
        }
      switch(Error)                             // Critical errors
        {
         case 2 : Print("Common error.");
            break;                              // Exit 'switch'
         case 5 : Print("Outdated version of the client terminal.");
            break;                              // Exit 'switch'
         case 64: Print("The account is blocked.");
            break;                              // Exit 'switch'
         case 133:Print("Trading forbidden");
            break; 
         case 138:
               Print("Occurred error ",Error, " try again.");
               openOrder(buy);
             break;                                // Exit 'switch'
         default: Print("Occurred error ",Error);// Other alternatives   
        }
      break;                                    // Exit cycle
     }
//-------------------------------------------------------------------------- 9 --
   return;                                      // Exit start()
  }
//-------------------------------------------------------------------------- 10 --
 
 
 string fileOpen() {
   ResetLastError();
   string str; 
   if (FileIsExist(Symbol()+ "-" + Period() + FileName,FILE_CSV|FILE_READ|FILE_WRITE)) {
      int file_handle=FileOpen(Symbol()+ "-" + Period() + FileName,FILE_READ|FILE_TXT|FILE_ANSI);
      if(file_handle!=INVALID_HANDLE) {
         PrintFormat("%s file is available for reading",FileName);
         PrintFormat("File path: %s\\Files\\",TerminalInfoString(TERMINAL_DATA_PATH));
         //--- additional variables
         int    str_size;
         
         //--- read data from the file
         while(!FileIsEnding(file_handle)) {
            //--- find out how many symbols are used for writing the time
            str_size=FileReadInteger(file_handle,INT_VALUE);
            //--- read the string
            str=FileReadString(file_handle,str_size);
            //--- print the string
            PrintFormat(str);
         }
         //--- close the file
         FileClose(file_handle);
         PrintFormat("Data is read, %s file is closed",FileName);
      } else {
         PrintFormat("Failed to open %s file, Error code = %d",FileName ,GetLastError());
      } 
      
      
      FileDelete(Symbol()+ "-" + Period() + FileName,FILE_CSV|FILE_READ|FILE_WRITE);   
   }   
   return str;   
 
 }
