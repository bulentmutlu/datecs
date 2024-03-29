// Copyright (C) 2005-2007 Oscar Sanderson                                    

#include "dl_iso8583_common.h"

DL_ERR _DL_ISO8583_MSG_AllocField ( DL_UINT16        iField,DL_UINT16        iSize,DL_ISO8583_MSG  *ioMsg,DL_UINT8       **oPtr )
{
    DL_ERR    err    = kDL_ERR_NONE;
    DL_UINT8 *tmpPtr = NULL;

    *oPtr = NULL;

    if ( ioMsg->sPtrNext ) /* static mode */
    {
            /* we cannot over-write an existing entry in static mode */
            if ( NULL != ioMsg->field[iField].ptr ) /* error */
            {
                    return kDL_ERR_OTHER;
            }
            else /* ok */
            {
                    /* allocate from static buffer */
                    if ( (ioMsg->sPtrNext + iSize + 1) > ioMsg->sPtrEnd ) /* error */
                    {
                            return kDL_ERR_OTHER;
                    }
                    else /* ok */
                    {
                            ioMsg->field[iField].len  = iSize;
                            tmpPtr                    = ioMsg->sPtrNext;
                            ioMsg->field[iField].ptr  = tmpPtr;
                            ioMsg->sPtrNext          += iSize + 1;
                    }
            }
    }
    else /* dynamic mode */
    {
            err = DL_MEM_malloc(iSize+1,(void      **)&tmpPtr);

            if ( !err )
            {
                    DL_MEM_free(ioMsg->field[iField].ptr);
                    ioMsg->field[iField].len = iSize;
                    ioMsg->field[iField].ptr = tmpPtr;
            }
    }

    *oPtr = tmpPtr;

    return err;
}

/******************************************************************************/

void DL_ISO8583_COMMON_SetHandler(DL_ISO8583_FIELD_DEF *iFieldDefArr, DL_UINT8 iFieldDefItems, DL_ISO8583_HANDLER  *oData )
{
    /* init outputs */
    DL_MEM_memset((void*)oData,0,sizeof(DL_ISO8583_HANDLER));

    /* set fields */
    oData->fieldArr   = iFieldDefArr;
    oData->fieldItems = iFieldDefItems;

    return;
}

